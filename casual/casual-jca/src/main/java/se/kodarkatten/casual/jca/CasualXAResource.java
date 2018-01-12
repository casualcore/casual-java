package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.kodarkatten.casual.network.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.UUID;

/**
 * @author jone
 */
public class CasualXAResource implements XAResource
{

    private final CasualManagedConnection casualManagedConnection;
    private Xid currentXid = XID.of();

    public CasualXAResource(final CasualManagedConnection connection)
    {
        casualManagedConnection = connection;
    }

    public Xid getCurrentXid()
    {
        return currentXid;
    }

    @Override
    public void commit(Xid xid, boolean onePhaseCommit) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        if (onePhaseCommit)
        {
            flags = Flag.of(XAFlags.TMONEPHASE);
        }
        Integer resourceId = CasualTransactionResources.getInstance().getResourceIdForXid(xid);
        CasualTransactionResourceCommitRequestMessage commitRequest =
                CasualTransactionResourceCommitRequestMessage.of(UUID.randomUUID(), xid, resourceId, flags);
        CasualTransactionResources.getInstance().removeResourceIdForXid(xid);
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> requestEnvelope = CasualNWMessage.of(UUID.randomUUID(), commitRequest);
        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> replyEnvelope = casualManagedConnection.getNetworkConnection().requestReply(requestEnvelope);
        CasualTransactionResourceCommitReplyMessage replyMsg = replyEnvelope.getMessage();

        throwWhenTransactionErrorCode( replyMsg.getTransactionReturnCode() );
    }

    /**
     * Removes the
     *
     * @param xid
     * @param flag - TMSUCCESS, TMFAIL, or TMSUSPEND.
     * @throws XAException
     */
    @Override
    public void end(Xid xid, int flag) throws XAException
    {
        reset();
        XAFlags f = XAFlags.unmarshall(flag);
        switch(f)
        {
            // note: we want to fallthrough here
            case TMSUCCESS:
            case TMFAIL:
            case TMSUSPEND:
                break;
            default:
                throw new XAException(XAException.XAER_RMFAIL);
        }
    }

    /**
     * Removes the records for a heuristically completed
     * transaction
     *
     * @param xid - ID of heuristically complete transaction
     * @throws XAException - Possible exception values are XAER_RMERR, XAER_RMFAIL, XAER_NOTA, XAER_INVAL, or XAER_PROTO.
     */
    @Override
    public void forget(Xid xid) throws XAException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int getTransactionTimeout() throws XAException
    {
        return 0;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        return false;
    }

    @Override
    public int prepare(Xid xid) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        Integer resourceId = CasualTransactionResources.getInstance().getResourceIdForXid(xid);
        CasualTransactionResourcePrepareRequestMessage prepareRequest = CasualTransactionResourcePrepareRequestMessage.of(UUID.randomUUID(),xid,resourceId,flags);
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> requestEnvelope = CasualNWMessage.of(UUID.randomUUID(), prepareRequest);
        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> replyEnvelope = casualManagedConnection.getNetworkConnection().requestReply(requestEnvelope);
        CasualTransactionResourcePrepareReplyMessage replyMsg = replyEnvelope.getMessage();

        throwWhenTransactionErrorCode( replyMsg.getTransactionReturnCode() );

        return replyMsg.getTransactionReturnCode().getId();
    }

    @Override
    public Xid[] recover(int i) throws XAException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void rollback(Xid xid) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        Integer resourceId = CasualTransactionResources.getInstance().getResourceIdForXid(xid);
        CasualTransactionResourceRollbackRequestMessage request =
                CasualTransactionResourceRollbackRequestMessage.of(UUID.randomUUID(), xid, resourceId, flags);
        CasualTransactionResources.getInstance().removeResourceIdForXid(xid);
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> requestEnvelope = CasualNWMessage.of(UUID.randomUUID(), request);
        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyEnvelope = casualManagedConnection.getNetworkConnection().requestReply(requestEnvelope);
        CasualTransactionResourceRollbackReplyMessage replyMsg = replyEnvelope.getMessage();
        throwWhenTransactionErrorCode( replyMsg.getTransactionReturnCode() );
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException
    {
        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException
    {
        if(!(XAFlags.TMJOIN.getValue() == i || XAFlags.TMRESUME.getValue() == i) &&
            CasualTransactionResources.getInstance().xidPending(xid))
        {
            throw new XAException(XAException.XAER_DUPID);
        }
        currentXid = xid;
    }

    @Override
    public String toString()
    {
        return "CasualXAResource{" +
            "currentXid=" + currentXid +
            '}';
    }

    public void reset()
    {
        currentXid = XID.of();
    }

    private void throwWhenTransactionErrorCode(final XAReturnCode transactionReturnCode) throws XAException
    {
        switch( transactionReturnCode )
        {
            case XA_OK:
            case XA_RDONLY:
                break;
            default:
                throw new XAException( transactionReturnCode.getId());
        }
    }

}
