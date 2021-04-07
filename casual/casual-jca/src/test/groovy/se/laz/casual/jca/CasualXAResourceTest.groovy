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
import se.laz.casual.network.grpc.MessageCreator
import se.laz.casual.network.messages.CasualCommitReply
import se.laz.casual.network.messages.CasualCommitRequest
import se.laz.casual.network.messages.CasualPrepareReply
import se.laz.casual.network.messages.CasualPrepareRequest
import se.laz.casual.network.messages.CasualReply
import se.laz.casual.network.messages.CasualRequest
import se.laz.casual.network.messages.CasualRollbackReply
import se.laz.casual.network.messages.CasualRollbackRequest
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
    @Shared CasualRequest expectedPrepareRequestMessage
    @Shared CasualRequest actualPrepareRequestMessage
    @Shared CasualReply prepareReplyMessage
    @Shared CasualRequest expectedRollbackRequestMessage
    @Shared CasualRequest actualRollbackRequestMessage
    @Shared CasualReply rollbackReplyMessage
    @Shared CasualRequest expectedCommitRequestMessage
    @Shared CasualRequest actualCommitRequestMessage
    @Shared CasualReply commitReplyMessage
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

    CasualRequest createPrepareRequestMessage(Xid xid, Flag<XAFlags> flags )
    {
        CasualPrepareRequest request = CasualPrepareRequest.newBuilder()
                .setXid(MessageCreator.toXID(xid1))
                .setResourceManagerId(mcf.getResourceId())
                .setXaFlags(flags.getFlagValue())
                .build()
        return CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.PREPARE_REQUEST)
                .setPrepare(request)
                .build()
    }

    CasualRequest createRollbackRequestMessage(Xid xid, Flag<XAFlags> flags )
    {
        CasualRollbackRequest request = CasualRollbackRequest.newBuilder()
                .setXid(MessageCreator.toXID(xid1))
                .setResourceManagerId(mcf.getResourceId())
                .setXaFlags(flags.getFlagValue())
                .build()
        return CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.ROLLBACK_REQUEST)
                .setRollback(request)
                .build()
    }

    CasualRequest createCommitRequestMessage(Xid xid, Flag<XAFlags> flags )
    {
        println "flag value: ${flags.getFlagValue()}"
        CasualCommitRequest request = CasualCommitRequest.newBuilder()
                .setXid(MessageCreator.toXID(xid1))
                .setResourceManagerId(mcf.getResourceId())
                .setXaFlags(flags.getFlagValue())
                .build()
        return CasualRequest.newBuilder()
                .setMessageType(CasualRequest.MessageType.COMMIT_REQUEST)
                .setCommit(request)
                .build()
    }

    def initialiseReplies()
    {
        prepareReplyMessage = createPrepareReplyMessage( xid1, XAReturnCode.XA_OK )
        rollbackReplyMessage = createRollbackReplyMessage( xid1, XAReturnCode.XA_OK )
        commitReplyMessage = createCommitReplyMessage( xid1, XAReturnCode.XA_OK )
    }

    CasualReply createPrepareReplyMessage(Xid xid, XAReturnCode returnCode )
    {
        CasualPrepareReply reply = CasualPrepareReply.newBuilder()
                .setXid(MessageCreator.toXID(xid1))
                .setResourceManagerId(mcf.getResourceId())
                .setXaReturnCode(MessageCreator.toXAReturnCode(returnCode))
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.PREPARE_REPLY)
                .setPrepare(reply)
                .build()
    }

    CasualReply createRollbackReplyMessage(Xid xid, XAReturnCode returnCode )
    {
        CasualRollbackReply reply = CasualRollbackReply.newBuilder()
                .setXid(MessageCreator.toXID(xid1))
                .setResourceManagerId(mcf.getResourceId())
                .setXaReturnCode(MessageCreator.toXAReturnCode(returnCode))
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.ROLLBACK_REPLY)
                .setRollback(reply)
                .build()
    }

    CasualReply createCommitReplyMessage(Xid xid, XAReturnCode returnCode )
    {
        CasualCommitReply reply = CasualCommitReply.newBuilder()
                .setXid(MessageCreator.toXID(xid1))
                .setResourceManagerId(mcf.getResourceId())
                .setXaReturnCode(MessageCreator.toXAReturnCode(returnCode))
                .build()
        return CasualReply.newBuilder()
                .setMessageType(CasualReply.MessageType.COMMIT_REPLY)
                .setCommit(reply)
                .build()
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

    def "Commit one phase false. returns ok."()
    {
        when:
        instance.commit( xid1, false )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualRequest input ->
                actualCommitRequestMessage = input
                return new CompletableFuture<>(commitReplyMessage)
        }
        CasualCommitRequest commitRequest = actualCommitRequestMessage.getCommit()
        MessageCreator.toXID(commitRequest.getXid()) == xid1
        commitRequest.getXaFlags() == Flag.of( XAFlags.TMNOFLAGS ).getFlagValue()
    }

    def "Commit one phase true, returns ok."()
    {
        when:
        instance.commit( xid1, true )

        then:
        noExceptionThrown()
        1 * networkConnection.request( _ ) >> {
            CasualRequest input ->
                actualCommitRequestMessage = input
                return new CompletableFuture<>(commitReplyMessage)
        }
        CasualCommitRequest commitRequest = actualCommitRequestMessage.getCommit()
        MessageCreator.toXID(commitRequest.getXid()) == xid1
        commitRequest.getXaFlags() == Flag.of( XAFlags.TMONEPHASE ).getFlagValue()
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
            CasualRequest input ->
                actualCommitRequestMessage = input
                return new CompletableFuture<>(commitReplyMessage)
        }
        CasualCommitRequest commitRequest = actualCommitRequestMessage.getCommit()
        MessageCreator.toXID(commitRequest.getXid()) == xid1
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
            CasualRequest input ->
                actualCommitRequestMessage = input
                return new CompletableFuture<>(commitReplyMessage)
        }
        CasualCommitRequest commitRequest = actualCommitRequestMessage.getCommit()
        MessageCreator.toXID(commitRequest.getXid()) == xid1
    }


    def "Forget is not supported."()
    {
        when:
        instance.forget( xid1 )

        then:
        thrown UnsupportedOperationException
    }

    def "IsSameRM"()
    {
        expect:
        instance.isSameRM( instance ) == false
    }

    def "Prepare returns ok."()
    {
        when:
        int reply = instance.prepare( xid1 )

        then:
        reply == XAReturnCode.XA_OK.getId()

        1 * networkConnection.request( _ ) >> {
            CasualRequest input ->
                actualPrepareRequestMessage = input
                return new CompletableFuture<>(prepareReplyMessage)
        }
        CasualPrepareRequest actualPrepareRequest = actualPrepareRequestMessage.getPrepare()
        MessageCreator.toXID(actualPrepareRequest.getXid()) == xid1
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
            CasualRequest input ->
                actualPrepareRequestMessage = input
                return new CompletableFuture<>(prepareReplyMessage)
        }
        CasualPrepareRequest actualPrepareRequest = actualPrepareRequestMessage.getPrepare()
        MessageCreator.toXID(actualPrepareRequest.getXid()) == xid1
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
            CasualRequest input ->
                actualPrepareRequestMessage = input
                return new CompletableFuture<>(prepareReplyMessage)
        }
        CasualPrepareRequest actualPrepareRequest = actualPrepareRequestMessage.getPrepare()
        MessageCreator.toXID(actualPrepareRequest.getXid()) == xid1
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
            CasualRequest input ->
                actualRollbackRequestMessage = input
                return new CompletableFuture<>(rollbackReplyMessage)
        }
        CasualRollbackRequest actualRollbackRequest = actualRollbackRequestMessage.getRollback()
        MessageCreator.toXID(actualRollbackRequest.getXid()) == xid1
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
            CasualRequest input ->
                actualRollbackRequestMessage = input
                return new CompletableFuture<>(rollbackReplyMessage)
        }
        CasualRollbackRequest actualRollbackRequest = actualRollbackRequestMessage.getRollback()
        MessageCreator.toXID(actualRollbackRequest.getXid()) == xid1
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
            CasualRequest input ->
                actualRollbackRequestMessage = input
                return new CompletableFuture<>(rollbackReplyMessage)
        }
        CasualRollbackRequest actualRollbackRequest = actualRollbackRequestMessage.getRollback()
        MessageCreator.toXID(actualRollbackRequest.getXid()) == xid1
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
