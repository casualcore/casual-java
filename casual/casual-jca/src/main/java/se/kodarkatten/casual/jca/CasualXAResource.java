package se.kodarkatten.casual.jca;

import se.kodarkatten.casual.api.flags.XAFlags;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * @author jone
 */
public class CasualXAResource implements XAResource
{

    private final CasualManagedConnection casualManagedConnection;

    public CasualXAResource(final CasualManagedConnection connection)
    {
        casualManagedConnection = connection;
    }

    @Override
    public void commit(Xid xid, boolean onePhaseCommit) throws XAException
    {
        casualManagedConnection.commitRequest(xid,onePhaseCommit);
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
        XAFlags f = XAFlags.unmarshall(flag);
        switch(f)
        {
            case TMSUCCESS:
                break;
            case TMFAIL:
                break;
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
        return casualManagedConnection.prepareRequest(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void rollback(Xid xid) throws XAException
    {
        casualManagedConnection.rollbackRequest(xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException
    {
        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException
    {
        casualManagedConnection.start(xid, i);
    }
}
