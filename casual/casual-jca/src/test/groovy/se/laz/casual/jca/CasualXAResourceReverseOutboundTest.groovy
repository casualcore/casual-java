/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.api.xa.XID
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import se.laz.casual.config.ReverseOutbound
import se.laz.casual.jca.pool.NetworkConnectionPool
import se.laz.casual.jca.pool.NetworkPoolHandler
import se.laz.casual.network.outbound.NettyNetworkConnection
import spock.lang.Specification

import javax.transaction.xa.XAException
import javax.transaction.xa.XAResource
import javax.transaction.xa.Xid
import java.nio.charset.StandardCharsets

class CasualXAResourceReverseOutboundTest extends Specification
{
    String poolName
    DomainId domainA
    DomainId domainB
    Xid xidA
    Xid xidB
    CasualManagedConnectionFactory reverseMcf
    NetworkConnectionPool reversePool
    Map<CasualXAResource, Xid> startedResources = [:]

    def setup()
    {
        NetworkPoolHandler.getInstance().@pools.clear()
        poolName = "reverse-xa-pool-${UUID.randomUUID()}"
        ConfigurationService.setConfiguration(
                ConfigurationOptions.CASUAL_REVERSE_OUTBOUND_INSTANCES,
                [ReverseOutbound.newBuilder().withName(poolName).withPort(7785).build()]
        )

        domainA = DomainId.of(UUID.randomUUID())
        domainB = DomainId.of(UUID.randomUUID())
        xidA = XID.of('global-tx-1'.getBytes(StandardCharsets.UTF_8), 'branch-a'.getBytes(StandardCharsets.UTF_8), 0)
        xidB = XID.of('global-tx-1'.getBytes(StandardCharsets.UTF_8), 'branch-b'.getBytes(StandardCharsets.UTF_8), 0)

        reversePool = NetworkPoolHandler.getInstance().getOrCreateReversePool(poolName)
        reversePool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
            getDomainId() >> domainA
            isActive() >> true
        })
        reversePool.addConnectionForReversePool(Mock(NettyNetworkConnection) {
            getDomainId() >> domainB
            isActive() >> true
        })

        reverseMcf = new CasualManagedConnectionFactory()
        reverseMcf.setHostName('reverse')
        reverseMcf.setPortNumber(0)
        reverseMcf.setNetworkConnectionPoolName(poolName)
    }

    def cleanup()
    {
        startedResources.each { resource, xid ->
            try
            {
                resource.end(xid, XAResource.TMSUCCESS)
            }
            catch (Exception ignored)
            {
                // Best-effort cleanup when a test fails mid-transaction.
            }
        }
        startedResources.clear()
        ConfigurationService.setConfiguration(ConfigurationOptions.CASUAL_REVERSE_OUTBOUND_INSTANCES, [])
        NetworkPoolHandler.getInstance().@pools.clear()
    }

    def 'isSameRM returns false for reverse outbound connections pinned to different domains on the same pool'()
    {
        given:
        CasualManagedConnection mcA = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcB = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainB)) as CasualManagedConnection
        mcA.getConnection(null, CasualRequestInfo.of(domainA))
        mcB.getConnection(null, CasualRequestInfo.of(domainB))
        CasualXAResource xaA = mcA.getXAResource() as CasualXAResource
        CasualXAResource xaB = mcB.getXAResource() as CasualXAResource

        expect:
        !xaA.isSameRM(xaB)
        !xaB.isSameRM(xaA)
    }

    def 'isSameRM returns true for reverse outbound connections pinned to the same domain on the same pool'()
    {
        given:
        CasualManagedConnection mcA1 = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcA2 = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        mcA1.getConnection(null, CasualRequestInfo.of(domainA))
        mcA2.getConnection(null, CasualRequestInfo.of(domainA))
        CasualXAResource xaA1 = mcA1.getXAResource() as CasualXAResource
        CasualXAResource xaA2 = mcA2.getXAResource() as CasualXAResource

        expect:
        xaA1.isSameRM(xaA2)
        xaA2.isSameRM(xaA1)
    }

    def 'isSameRM distinguishes domains after createManagedConnection before getConnection is called'()
    {
        given: 'JCA/IronJacamar enlists the XAResource before calling getConnection on a new managed connection'
        CasualManagedConnection mcA1 = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcA2 = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcB = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainB)) as CasualManagedConnection
        CasualXAResource xaA1 = mcA1.getXAResource() as CasualXAResource
        CasualXAResource xaA2 = mcA2.getXAResource() as CasualXAResource
        CasualXAResource xaB = mcB.getXAResource() as CasualXAResource

        expect:
        mcA1.getPinnedDomainId() == Optional.of(domainA)
        mcB.getPinnedDomainId() == Optional.of(domainB)
        !xaA1.isSameRM(xaB)
        !xaB.isSameRM(xaA1)
        xaA1.isSameRM(xaA2)
    }

    def 'isSameRM returns false between a pinned reverse outbound connection and an unpinned normal outbound connection'()
    {
        given:
        CasualManagedConnectionFactory normalMcf = new CasualManagedConnectionFactory()
        normalMcf.setHostName('reverse')
        normalMcf.setPortNumber(0)
        normalMcf.setNetworkConnectionPoolName('normal-pool')
        normalMcf.setNetworkConnectionPoolSize(1)

        CasualManagedConnection reverseMc = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        reverseMc.getConnection(null, CasualRequestInfo.of(domainA))
        CasualManagedConnection normalMc = normalMcf.createManagedConnection(null, null) as CasualManagedConnection

        CasualXAResource reverseXa = reverseMc.getXAResource() as CasualXAResource
        CasualXAResource normalXa = normalMc.getXAResource() as CasualXAResource

        expect:
        !reverseXa.isSameRM(normalXa)
        !normalXa.isSameRM(reverseXa)
    }

    def 'reverse outbound start and end track pending transaction branches per pinned domain'()
    {
        given:
        CasualManagedConnection mcA1 = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcA2 = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcB = reverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainB)) as CasualManagedConnection
        mcA1.getConnection(null, CasualRequestInfo.of(domainA))
        mcA2.getConnection(null, CasualRequestInfo.of(domainA))
        mcB.getConnection(null, CasualRequestInfo.of(domainB))

        CasualXAResource xaA1 = mcA1.getXAResource() as CasualXAResource
        CasualXAResource xaA2 = mcA2.getXAResource() as CasualXAResource
        CasualXAResource xaB = mcB.getXAResource() as CasualXAResource
        ResourceId rmA = ResourceId.of(domainA)
        ResourceId rmB = ResourceId.of(domainB)

        when: 'distinct branch XIDs for the same global transaction are started on two reverse outbound domains'
        xaA1.start(xidA, XAResource.TMNOFLAGS)
        startedResources[xaA1] = xidA
        xaB.start(xidB, XAResource.TMNOFLAGS)
        startedResources[xaB] = xidB

        then: 'each branch is tracked solely under its own pinned domain'
        noExceptionThrown()
        CasualResourceManager.getInstance().isPending(rmA, xidA)
        !CasualResourceManager.getInstance().isPending(rmB, xidA)
        CasualResourceManager.getInstance().isPending(rmB, xidB)
        !CasualResourceManager.getInstance().isPending(rmA, xidB)
        CasualResourceManager.getInstance().getPendingTransactionCount() == 2

        when: 'starting the same branch XID again on the same domain without TMJOIN/TMRESUME'
        xaA2.start(xidA, XAResource.TMNOFLAGS)

        then:
        XAException duplicate = thrown()
        duplicate.errorCode == XAException.XAER_DUPID

        when: 'ending domainA leaves domainB pending until domainB also ends'
        xaA1.end(xidA, XAResource.TMSUCCESS)

        then:
        !CasualResourceManager.getInstance().isPending(rmA, xidA)
        CasualResourceManager.getInstance().isPending(rmB, xidB)
        CasualResourceManager.getInstance().getPendingTransactionCount() == 1

        when:
        xaB.end(xidB, XAResource.TMSUCCESS)

        then:
        !CasualResourceManager.getInstance().isPending(rmB, xidB)
        CasualResourceManager.getInstance().getPendingTransactionCount() == 0
    }

    def 'reverse outbound XA operations work without hostName or portNumber configured'()
    {
        given:
        CasualManagedConnectionFactory hostlessReverseMcf = new CasualManagedConnectionFactory()
        hostlessReverseMcf.setNetworkConnectionPoolName(poolName)

        CasualManagedConnection mcA = hostlessReverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainA)) as CasualManagedConnection
        CasualManagedConnection mcB = hostlessReverseMcf.createManagedConnection(null, CasualRequestInfo.of(domainB)) as CasualManagedConnection
        CasualXAResource xaA = mcA.getXAResource() as CasualXAResource
        CasualXAResource xaB = mcB.getXAResource() as CasualXAResource
        ResourceId rmA = ResourceId.of(domainA)
        ResourceId rmB = ResourceId.of(domainB)

        when:
        boolean sameRm = xaA.isSameRM(xaB)
        xaA.start(xidA, XAResource.TMNOFLAGS)
        startedResources[xaA] = xidA
        xaB.start(xidB, XAResource.TMNOFLAGS)
        startedResources[xaB] = xidB
        boolean pendingOnA = CasualResourceManager.getInstance().isPending(rmA, xidA)
        boolean pendingOnB = CasualResourceManager.getInstance().isPending(rmB, xidB)
        long pendingDuringTx = CasualResourceManager.getInstance().getPendingTransactionCount()
        xaA.end(xidA, XAResource.TMSUCCESS)
        xaB.end(xidB, XAResource.TMSUCCESS)

        then:
        !sameRm
        pendingOnA
        pendingOnB
        pendingDuringTx == 2
        !CasualResourceManager.getInstance().isPending(rmA, xidA)
        !CasualResourceManager.getInstance().isPending(rmB, xidB)
        CasualResourceManager.getInstance().getPendingTransactionCount() == 0
    }
}
