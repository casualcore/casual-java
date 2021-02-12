/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca;

import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.XAFlags;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.api.xa.XAReturnCode;
import se.laz.casual.api.xa.XID;
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceCommitRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourcePrepareRequestMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackReplyMessage;
import se.laz.casual.network.protocol.messages.transaction.CasualTransactionResourceRollbackRequestMessage;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author jone
 */
public class CasualXAResource implements XAResource
{
    private static final Xid[] NO_XIDS = {};
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

        CasualNWMessage<CasualTransactionResourceCommitReplyMessage> replyEnvelope = replyEnvelopeFuture.join();
        CasualTransactionResourceCommitReplyMessage replyMsg = replyEnvelope.getMessage();
        throwWhenTransactionErrorCode(replyMsg.getTransactionReturnCode());

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
        disassociate();
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
        return casualManagedConnection.getTransactionTimeout();
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

        CasualNWMessage<CasualTransactionResourcePrepareReplyMessage> replyEnvelope = replyEnvelopeFuture.join();
        CasualTransactionResourcePrepareReplyMessage replyMsg = replyEnvelope.getMessage();
        throwWhenTransactionErrorCode(replyMsg.getTransactionReturnCode());
        return replyMsg.getTransactionReturnCode().getId();
    }

    @Override
    public Xid[] recover(int i) throws XAException
    {
        return NO_XIDS;
    }

    @Override
    public void rollback(Xid xid) throws XAException
    {
        Flag<XAFlags> flags = Flag.of(XAFlags.TMNOFLAGS);
        CasualTransactionResourceRollbackRequestMessage request =
                CasualTransactionResourceRollbackRequestMessage.of(UUID.randomUUID(), xid, resourceManagerId, flags);
        CasualNWMessage<CasualTransactionResourceRollbackRequestMessage> requestEnvelope = CasualNWMessageImpl.of(UUID.randomUUID(), request);
        CompletableFuture<CasualNWMessage<CasualTransactionResourceRollbackReplyMessage>> replyEnvelopeFuture = casualManagedConnection.getNetworkConnection().request(requestEnvelope);

        CasualNWMessage<CasualTransactionResourceRollbackReplyMessage> replyEnvelope = replyEnvelopeFuture.join();
        CasualTransactionResourceRollbackReplyMessage replyMsg = replyEnvelope.getMessage();
        throwWhenTransactionErrorCode(replyMsg.getTransactionReturnCode());
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException
    {
        casualManagedConnection.setTransactionTimeout(i);
        return true;
    }

    @Override
    public void start(Xid xid, int i) throws XAException
    {
        if(!(XAFlags.TMJOIN.getValue() == i || XAFlags.TMRESUME.getValue() == i) &&
            CasualResourceManager.getInstance().isPending(xid))
        {
            throw new XAException(XAException.XAER_DUPID);
        }
        associate(xid);
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

    private void associate(Xid xid)
    {
        currentXid = xid;
    }

    public void disassociate()
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
