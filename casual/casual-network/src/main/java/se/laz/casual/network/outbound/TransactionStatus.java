package se.laz.casual.network.outbound;

import javax.transaction.Status;

public enum TransactionStatus
{
    STATUS_ACTIVE(Status.STATUS_ACTIVE),
    STATUS_COMMITTED(Status.STATUS_COMMITTED),
    STATUS_COMMITTING(Status.STATUS_COMMITTING),
    STATUS_MARKED_ROLLBACK(Status.STATUS_MARKED_ROLLBACK),
    STATUS_NO_TRANSACTION(Status.STATUS_NO_TRANSACTION),
    STATUS_PREPARED(Status.STATUS_PREPARED),
    STATUS_PREPARING(Status.STATUS_PREPARING),
    STATUS_ROLLEDBACK(Status.STATUS_ROLLEDBACK),
    STATUS_ROLLING_BACK(Status.STATUS_ROLLING_BACK),
    STATUS_UNKNOWN(Status. 	STATUS_UNKNOWN);

    private int id;
    TransactionStatus(int id)
    {
        this.id = id;
    }

    public int getValue()
    {
        return id;
    }

    public static final TransactionStatus unmarshal(int id)
    {
        for (TransactionStatus c : TransactionStatus.values())
        {
            if (c.id == id)
            {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown TransactionStatus:" + id);
    }

}
