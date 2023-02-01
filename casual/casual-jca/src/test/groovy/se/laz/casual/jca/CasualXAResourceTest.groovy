/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca


import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.XAFlags
import se.laz.casual.api.xa.XAReturnCode
import se.laz.casual.api.xa.XID
import se.laz.casual.internal.network.NetworkConnection
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.transaction.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.transaction.xa.XAException
import javax.transaction.xa.XAResource
import javax.transaction.xa.Xid
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class CasualXAResourceTest extends Specification
{
    @Shared CasualManagedConnectionFactory mcf
    @Shared CasualXAResource instance
    @Shared CasualManagedConnection managedConnection
    @Shared NetworkConnection networkConnection
    @Shared Xid xid1, xid2
    @Shared CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> expectedPrepareRequestMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> actualPrepareRequestMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourcePrepareReplyMessage> prepareReplyMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> expectedRollbackRequestMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> actualRollbackRequestMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourceRollbackReplyMessage> rollbackReplyMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> expectedCommitRequestMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> actualCommitRequestMessage
    @Shared CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> commitReplyMessage
    @Shared CasualResourceManager transactionResources
    @Shared int resourceId = 42

    def setup()
    {
        mcf = Mock(CasualManagedConnectionFactory)
        mcf.getResourceId() >> {
            resourceId
        }
        networkConnection = Mock(NetworkConnection)
        managedConnection = new CasualManagedConnection( Mock(CasualManagedConnectionFactory) )
        managedConnection.networkConnection = networkConnection
        instance = new CasualXAResource( managedConnection, mcf.getResourceId() )

        xid1 = XID.of( "123".getBytes(StandardCharsets.UTF_8), "321".getBytes(StandardCharsets.UTF_8), 0 )
        xid2 = XID.of( "456".getBytes(StandardCharsets.UTF_8), "654".getBytes(StandardCharsets.UTF_8), 0 )

        transactionResources = CasualResourceManager.getInstance()

        initialiseExpectedRequests()
        initialiseReplies()
    }

    def cleanup()
    {
        transactionResources.remove( xid1 )
        transactionResources.remove( xid2 )
    }

    def initialiseExpectedRequests()
    {
        expectedPrepareRequestMessage = createPrepareRequestMessage( xid1, Flag.of(XAFlags.TMNOFLAGS) )
        expectedRollbackRequestMessage = createRollbackRequestMessage( xid1, Flag.of( XAFlags.TMNOFLAGS ) )
        expectedCommitRequestMessage = createCommitRequestMessage( xid1, Flag.of( XAFlags.TMNOFLAGS ) )
    }

    CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> createPrepareRequestMessage(Xid xid, Flag<XAFlags> flags )
    {
        return CasualNWMessageImpl.of(null,
                CasualTransactionResourcePrepareRequestMessage.of(null,
                        xid,
                        mcf.getResourceId(),
                        flags) )
    }

    CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> createRollbackRequestMessage(Xid xid, Flag<XAFlags> flags )
    {
        return CasualNWMessageImpl.of(null,
                CasualTransactionResourceRollbackRequestMessage.of(null,
                        xid,
                        mcf.getResourceId(),
                        flags) )
    }

    CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> createCommitRequestMessage(Xid xid, Flag<XAFlags> flags )
    {
        return CasualNWMessageImpl.of(null,
                CasualTransactionResourceCommitRequestMessage.of(null,
                        xid,
                        mcf.getResourceId(),
                        flags) )
    }

    def initialiseReplies()
    {
        prepareReplyMessage = createPrepareReplyMessage( xid1, XAReturnCode.XA_OK )
        rollbackReplyMessage = createRollbackReplyMessage( xid1, XAReturnCode.XA_OK )
        commitReplyMessage = createCommitReplyMessage( xid1, XAReturnCode.XA_OK )
    }

    CasualNWMessageImpl<CasualTransactionResourcePrepareReplyMessage> createPrepareReplyMessage(Xid xid, XAReturnCode returnCode )
    {
        CasualNWMessageImpl.of( null,
                CasualTransactionResourcePrepareReplyMessage.of( null,
                        xid,
                        mcf.getResourceId(),
                        returnCode
                )
        )
    }

    CasualNWMessageImpl<CasualTransactionResourceRollbackReplyMessage> createRollbackReplyMessage(Xid xid, XAReturnCode returnCode )
    {
        CasualNWMessageImpl.of( null,
                CasualTransactionResourceRollbackReplyMessage.of( null,
                        xid,
                        mcf.getResourceId(),
                        returnCode
                )
        )
    }

    CasualNWMessageImpl<CasualTransactionResourceCommitReplyMessage> createCommitReplyMessage(Xid xid, XAReturnCode returnCode )
    {
        CasualNWMessageImpl.of( null,
                CasualTransactionResourceCommitReplyMessage.of( null,
                        xid,
                        mcf.getResourceId(),
                        returnCode
                )
        )
    }


    def "GetCurrentXid is a null xid until start is called."()
    {
        expect:
        instance.getCurrentXid() == XID.NULL_XID
    }

    def "GetCurrentXid returns value given during start."()
    {
        when:
        transactionResources.remove( xid1 )
        instance.start( xid1, 0 )

        then:
        instance.getCurrentXid() == xid1
    }

    def "GetCurrentXid returns null xid after end has been called."()
    {
        when:
        transactionResources.remove( xid1 )
        instance.start( xid1, 0 )
        instance.end(xid1, XAFlags.TMSUCCESS.getValue())
        then:
        instance.getCurrentXid() == XID.NULL_XID
    }

    def "Start called with xid with already pending transaction throws XAException."()
    {
        setup:
        instance.start( xid1, 0 )

        when:
        instance.start( xid1, 0 )

        then:
        thrown XAException
    }

    @Unroll
    def "Start called with xid with already pending transaction though #flag throws no Exception."()
    {
        setup:
        instance.start( xid1, flag )

        when:
        instance.start( xid1, flag )

        then:
        noExceptionThrown()

        where:
        flag << [
            XAFlags.TMJOIN.getValue(),
            XAFlags.TMRESUME.getValue()
        ]

    }

    @Unroll
    def "End status resulting in no exception."()
    {
        when:
        instance.end( xid1, status )

        then:
        noExceptionThrown()

        where:
        status << [
        XAResource.TMSUCCESS,
        XAResource.TMFAIL,
        XAResource.TMSUSPEND ]
    }

    @Unroll
    def "End status resulting in XAException."()
    {
        when:
        instance.end( xid1, status )

        then:
        thrown expectedException

        where:
        status || expectedException
        XAResource.TMENDRSCAN || XAException
        XAResource.TMJOIN || XAException
        XAResource.TMNOFLAGS || XAException
        XAResource.TMONEPHASE || XAException
        XAResource.TMRESUME || XAException
        XAResource.TMSTARTRSCAN || XAException

    }

   def 'start after end with TMSUSPEND does not throw'()
   {
      given:
      def startStatus = XAFlags.TMNOFLAGS
      def endStatus = XAFlags.TMSUSPEND
      def startAgainStatus = XAFlags.TMRESUME
      when:
      instance.start(xid1, startStatus.value)
      then:
      noExceptionThrown()
      when:
      instance.end(xid1, endStatus.value)
      then:
      noExceptionThrown()
      when:
      instance.start(xid1, startAgainStatus.value)
      then:
      noExceptionThrown()
   }

    def "Commit one phase false. returns ok."()
    {
        when:
        instance.commit( xid1, false )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return CompletableFuture.completedFuture(commitReplyMessage)
        }

        actualCommitRequestMessage.getMessage().getXid() == xid1
        actualCommitRequestMessage.getMessage().getFlags() == Flag.of( XAFlags.TMNOFLAGS )
    }

    def "Commit one phase true, returns ok."()
    {
        when:
        instance.commit( xid1, true )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return CompletableFuture.completedFuture(commitReplyMessage)
        }

        actualCommitRequestMessage.getMessage().getXid() == xid1
        actualCommitRequestMessage.getMessage().getFlags() == Flag.of( XAFlags.TMONEPHASE )
    }

    def "Commit returns read only."()
    {
        setup:
        commitReplyMessage = createCommitReplyMessage( xid1, XAReturnCode.XA_RDONLY )

        when:
        instance.commit( xid1, false )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return CompletableFuture.completedFuture(commitReplyMessage)
        }

        actualCommitRequestMessage.getMessage().getXid() == xid1
    }

    def "Commit returns fail, throws XAException."()
    {
        setup:
        commitReplyMessage = createCommitReplyMessage( xid1, XAReturnCode.XAER_RMFAIL )

        when:
        instance.commit( xid1, false )

        then:
        thrown XAException
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return CompletableFuture.completedFuture(commitReplyMessage)
        }

        actualCommitRequestMessage.getMessage().getXid() == xid1
    }


    def "Forget is not supported."()
    {
        when:
        instance.forget( xid1 )

        then:
        thrown UnsupportedOperationException
    }

    def "isSameRM"()
    {
       setup:
       def rmIdOne = 1
       DomainId firstDomainId = DomainId.of(UUID.randomUUID())
       CasualManagedConnection casualManagedConnectionOne = Mock(CasualManagedConnection) {
           getDomainId() >> {
              firstDomainId
           }
       }

       def rmIdTwo = 2
       DomainId secondDomainId = DomainId.of(UUID.randomUUID())
       CasualManagedConnection casualManagedConnectionTwo = Mock(CasualManagedConnection) {
           getDomainId() >> {
              secondDomainId
           }
       }

       def rmIdThree = 3
       CasualManagedConnection casualManagedConnectionThree = Mock(CasualManagedConnection) {
           getDomainId() >> {
              firstDomainId
           }
       }
       when:
       CasualXAResource xaResourceOne = new CasualXAResource(casualManagedConnectionOne, rmIdOne)
       CasualXAResource xaResourceTwo = new CasualXAResource(casualManagedConnectionTwo, rmIdTwo)
       CasualXAResource xaResourceThree = new CasualXAResource(casualManagedConnectionThree, rmIdThree)

       then:
       !xaResourceOne.isSameRM(xaResourceTwo)
       xaResourceOne.isSameRM(xaResourceThree)
       !xaResourceTwo.isSameRM(xaResourceThree)
    }

    def "Prepare returns ok."()
    {
        when:
        int reply = instance.prepare( xid1 )

        then:
        reply == XAReturnCode.XA_OK.getId()

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> input ->
                actualPrepareRequestMessage = input
                return CompletableFuture.completedFuture(prepareReplyMessage)
        }

        actualPrepareRequestMessage.getMessage().getXid() == xid1
    }

    def "Prepare returns read only."()
    {
        setup:
        prepareReplyMessage = createPrepareReplyMessage( xid1, XAReturnCode.XA_RDONLY )

        when:
        int reply = instance.prepare( xid1 )

        then:
        reply == XAReturnCode.XA_RDONLY.getId()

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> input ->
                actualPrepareRequestMessage = input
               return CompletableFuture.completedFuture(prepareReplyMessage)
        }

        actualPrepareRequestMessage.getMessage().getXid() == xid1
    }

    def "Prepare returns fail, throws XAException."()
    {
        setup:
        prepareReplyMessage = createPrepareReplyMessage( xid1, XAReturnCode.XAER_RMFAIL )

        when:
        instance.prepare( xid1 )

        then:
        thrown XAException

        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourcePrepareRequestMessage> input ->
                actualPrepareRequestMessage = input
                return CompletableFuture.completedFuture(prepareReplyMessage)
        }

        actualPrepareRequestMessage.getMessage().getXid() == xid1
    }

    def "recover is returns empty array."()
    {
        when:
        def result = instance.recover( 0 )

        then:
        result.size() == 0
    }

    def "Rollback returns ok."()
    {
        when:
        instance.rollback( xid1 )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> input ->
                actualRollbackRequestMessage = input
                return CompletableFuture.completedFuture(rollbackReplyMessage)
        }

        actualRollbackRequestMessage.getMessage().getXid() == xid1
    }

    def "Rollback returns read only."()
    {
        setup:
        rollbackReplyMessage = createRollbackReplyMessage( xid1, XAReturnCode.XA_RDONLY )

        when:
        instance.rollback( xid1 )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> input ->
                actualRollbackRequestMessage = input
                return CompletableFuture.completedFuture(rollbackReplyMessage)
        }

        actualRollbackRequestMessage.getMessage().getXid() == xid1
    }

    def "Rollback returns fail, throws XAException."()
    {
        setup:
        rollbackReplyMessage = createRollbackReplyMessage( xid1, XAReturnCode.XAER_RMFAIL )

        when:
        instance.rollback( xid1 )

        then:
        thrown XAException
        1 * networkConnection.request( _ ) >> {
            CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> input ->
                actualRollbackRequestMessage = input
                return CompletableFuture.completedFuture(rollbackReplyMessage)
        }

        actualRollbackRequestMessage.getMessage().getXid() == xid1
    }

    def "getTransactionTimeout when not set returns default value."()
    {
        expect:
        instance.getTransactionTimeout() == 0
    }

    def "SetTransactionTimeout is allowed, returns true and does change value returned by getTransactionTimeout."()
    {
        setup:
        int defaultTimeout = instance.getTransactionTimeout()
        int timeout = 10

        when:
        boolean res = instance.setTransactionTimeout( timeout )

        then:
        res == true
        instance.getTransactionTimeout() == timeout
        instance.getTransactionTimeout() != defaultTimeout
    }

}
