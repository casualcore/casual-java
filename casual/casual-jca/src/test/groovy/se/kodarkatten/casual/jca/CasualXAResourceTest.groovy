package se.kodarkatten.casual.jca

import groovy.json.internal.Charsets
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.flags.XAFlags
import se.kodarkatten.casual.api.xa.XAReturnCode
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.transaction.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.transaction.xa.XAException
import javax.transaction.xa.XAResource
import javax.transaction.xa.Xid

class CasualXAResourceTest extends Specification
{
    @Shared CasualXAResource instance
    @Shared CasualManagedConnection managedConnection
    @Shared NetworkConnection networkConnection
    @Shared Xid xid1, xid2
    @Shared CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> expectedPrepareRequestMessage
    @Shared CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> actualPrepareRequestMessage
    @Shared CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> prepareReplyMessage
    @Shared CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> expectedRollbackRequestMessage
    @Shared CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> actualRollbackRequestMessage
    @Shared CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> rollbackReplyMessage
    @Shared CasualNWMessage<CasualTransactionResourceCommitRequestMessage> expectedCommitRequestMessage
    @Shared CasualNWMessage<CasualTransactionResourceCommitRequestMessage> actualCommitRequestMessage
    @Shared CasualNWMessage<CasualTransactionResourceCommitReplyMessage> commitReplyMessage
    @Shared CasualTransactionResources transactionResources

    def setup()
    {
        networkConnection = Mock(NetworkConnection)
        managedConnection = new CasualManagedConnection( null, null )
        managedConnection.networkConnection = networkConnection
        instance = new CasualXAResource( managedConnection )

        xid1 = XID.of( "123".getBytes(Charsets.UTF_8), "321".getBytes(Charsets.UTF_8), 0 )
        xid2 = XID.of( "456".getBytes(Charsets.UTF_8), "654".getBytes(Charsets.UTF_8), 0 )

        transactionResources = CasualTransactionResources.getInstance()

        initialiseExpectedRequests()
        initialiseReplies()
    }

    def cleanup()
    {
        transactionResources.removeResourceIdForXid( xid1 )
        transactionResources.removeResourceIdForXid( xid2 )
    }

    def initialiseExpectedRequests()
    {
        expectedPrepareRequestMessage = createPrepareRequestMessage( xid1, Flag.of(XAFlags.TMNOFLAGS) )
        expectedRollbackRequestMessage = createRollbackRequestMessage( xid1, Flag.of( XAFlags.TMNOFLAGS ) )
        expectedCommitRequestMessage = createCommitRequestMessage( xid1, Flag.of( XAFlags.TMNOFLAGS ) )
    }

    CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> createPrepareRequestMessage( Xid xid, Flag<XAFlags> flags )
    {
        return CasualNWMessage.of(null,
                CasualTransactionResourcePrepareRequestMessage.of(null,
                        xid,
                        transactionResources.getResourceIdForXid(xid),
                        flags) )
    }

    CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> createRollbackRequestMessage( Xid xid, Flag<XAFlags> flags )
    {
        return CasualNWMessage.of(null,
                CasualTransactionResourceRollbackRequestMessage.of(null,
                        xid,
                        transactionResources.getResourceIdForXid(xid),
                        flags) )
    }

    CasualNWMessage<CasualTransactionResourceCommitRequestMessage> createCommitRequestMessage( Xid xid, Flag<XAFlags> flags )
    {
        return CasualNWMessage.of(null,
                CasualTransactionResourceCommitRequestMessage.of(null,
                        xid,
                        transactionResources.getResourceIdForXid(xid),
                        flags) )
    }

    def initialiseReplies()
    {
        prepareReplyMessage = createPrepareReplyMessage( xid1, XAReturnCode.XA_OK )
        rollbackReplyMessage = createRollbackReplyMessage( xid1, XAReturnCode.XA_OK )
        commitReplyMessage = createCommitReplyMessage( xid1, XAReturnCode.XA_OK )
    }

    CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> createPrepareReplyMessage( Xid xid, XAReturnCode returnCode )
    {
        CasualNWMessage.of( null,
                CasualTransactionResourcePrepareReplyMessage.of( null,
                        xid,
                        transactionResources.getResourceIdForXid( xid ),
                        returnCode
                )
        )
    }

    CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> createRollbackReplyMessage( Xid xid, XAReturnCode returnCode )
    {
        CasualNWMessage.of( null,
                CasualTransactionResourceRollbackReplyMessage.of( null,
                        xid,
                        transactionResources.getResourceIdForXid( xid ),
                        returnCode
                )
        )
    }

    CasualNWMessage<CasualTransactionResourceCommitReplyMessage> createCommitReplyMessage( Xid xid, XAReturnCode returnCode )
    {
        CasualNWMessage.of( null,
                CasualTransactionResourceCommitReplyMessage.of( null,
                        xid,
                        transactionResources.getResourceIdForXid( xid ),
                        returnCode
                )
        )
    }


    def "GetCurrentXid is null until start is called."()
    {
        expect:
        instance.getCurrentXid() == null
    }

    def "GetCurrentXid returns value given during start."()
    {
        when:
        transactionResources.removeResourceIdForXid( xid1 )
        instance.start( xid1, 0 )

        then:
        instance.getCurrentXid() == xid1
    }

    def "Start called with xid with already pending transaction throws XAException."()
    {
        setup:
        CasualTransactionResources.getInstance().getResourceIdForXid( xid1 )

        when:
        instance.start( xid1, 0 )

        then:
        thrown XAException
    }

    @Unroll
    def "Start called with xid with already pending transaction though #flag throws no Exception."()
    {
        setup:
        CasualTransactionResources.getInstance().getResourceIdForXid( xid1 )

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
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return commitReplyMessage
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
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return commitReplyMessage
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
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return commitReplyMessage
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
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceCommitRequestMessage> input ->
                actualCommitRequestMessage = input
                return commitReplyMessage
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

        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> input ->
                actualPrepareRequestMessage = input
                return prepareReplyMessage
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

        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> input ->
                actualPrepareRequestMessage = input
                return prepareReplyMessage
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

        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> input ->
                actualPrepareRequestMessage = input
                return prepareReplyMessage
        }

        actualPrepareRequestMessage.getMessage().getXid() == xid1
    }

    def "Recover is not supported."()
    {
        when:
        instance.recover( 0 )

        then:
        thrown UnsupportedOperationException
    }

    def "Rollback returns ok."()
    {
        when:
        instance.rollback( xid1 )

        then:
        noExceptionThrown()
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> input ->
                actualRollbackRequestMessage = input
                return rollbackReplyMessage
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
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> input ->
                actualRollbackRequestMessage = input
                return rollbackReplyMessage
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
        1 * networkConnection.requestReply( _ ) >> {
            CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> input ->
                actualRollbackRequestMessage = input
                return rollbackReplyMessage
        }

        actualRollbackRequestMessage.getMessage().getXid() == xid1
    }

    def "GetTransactionTimeout when not set returns default value."()
    {
        expect:
        instance.getTransactionTimeout() == 0
    }

    def "SetTransactionTimeout is not allowed, returns false and does not change value returned by GetTransactionTimeout."()
    {
        setup:
        int currentTimeout = instance.getTransactionTimeout()
        int timeout = 10

        when:
        boolean res = instance.setTransactionTimeout( timeout )

        then:
        ! res
        instance.getTransactionTimeout() != timeout
        instance.getTransactionTimeout() == currentTimeout
    }
}
