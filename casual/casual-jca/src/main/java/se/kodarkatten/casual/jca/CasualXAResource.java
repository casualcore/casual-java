package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.XAFlags;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.protocol.connection.CasualConnectionException;
import se.kodarkatten.casual.network.protocol.messages.CasualNWMessageImpl;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.kodarkatten.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author jone
 */
public class CasualXAResource implements XAResource
{

    private final CasualManagedConnection casualManagedConnection;
    private final int resourceManagerId;
    private Xid currentXid = XID.NULL_XID;

    public CasualXAResource(final CasualManagedConnection connection, int resourceManagerId)
    {
        casualManagedConnection = connection;
        this.resourceManagerId = resourceManagerId;
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
        CasualTransactionResourceCommitRequestMessage commitRequest =
            CasualTransactionResourceCommitRequestMessage.of(UUID.randomUUID(), xid, resourceManagerId, flags);
        CasualNWMessage<CasualTransactionResourceCommitRequestMessage> requestEnvelope = CasualNWMessageImpl.of(UUID.randomUUID(), commitRequest);
        CompletableFuture<CasualNWMessage<CasualTransactionResourceCommitReplyMessage>> replyEnvelopeFuture = casualManagedConnection.getNetworkConnection().request(requestEnvelope);
        try
        {
            CasualNWMessage<CasualTransactionResourceCommitReplyMessage> replyEnvelope = replyEnvelopeFuture.get();
            CasualTransactionResourceCommitReplyMessage replyMsg = replyEnvelope.getMessage();
            throwWhenTransactionErrorCode(replyMsg.getTransactionReturnCode());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
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
        CasualResourceManager.getInstance().remove(xid);
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
        CasualTransactionResourcePrepareRequestMessage prepareRequest = CasualTransactionResourcePrepareRequestMessage.of(UUID.randomUUID(), xid, resourceManagerId, flags);
        CasualNWMessage<CasualTransactionResourcePrepareRequestMessage> requestEnvelope = CasualNWMessageImpl.of(UUID.randomUUID(), prepareRequest);
        CompletableFuture<CasualNWMessage<CasualTransactionResourcePrepareReplyMessage>> replyEnvelopeFuture = casualManagedConnection.getNetworkConnection().request(requestEnvelope);
        try
        {
            CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> replyEnvelope = replyEnvelopeFuture.get();
            CasualTransactionResourcePrepareReplyMessage replyMsg = replyEnvelope.getMessage();
            throwWhenTransactionErrorCode(replyMsg.getTransactionReturnCode());
            return replyMsg.getTransactionReturnCode().getId();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
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
        CasualTransactionResourceRollbackRequestMessage request =
                CasualTransactionResourceRollbackRequestMessage.of(UUID.randomUUID(), xid, resourceManagerId, flags);
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> requestEnvelope = CasualNWMessageImpl.of(UUID.randomUUID(), request);
        CompletableFuture<CasualNWMessage<CasualTransactionResourceRollbackReplyMessage>> replyEnvelopeFuture = casualManagedConnection.getNetworkConnection().request(requestEnvelope);
        try
        {
            CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyEnvelope = replyEnvelopeFuture.get();
            CasualTransactionResourceRollbackReplyMessage replyMsg = replyEnvelope.getMessage();
            throwWhenTransactionErrorCode(replyMsg.getTransactionReturnCode());
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new CasualConnectionException(e);
        }
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
            CasualResourceManager.getInstance().isPending(xid))
        {
            throw new XAException(XAException.XAER_DUPID);
        }
        currentXid = xid;
        if(!CasualResourceManager.getInstance().isPending(currentXid))
        {
            CasualResourceManager.getInstance().put(currentXid);
        }
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
        currentXid = XID.NULL_XID;
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
