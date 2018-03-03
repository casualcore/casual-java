/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.xa;

import javax.transaction.xa.XAException;

/**
 * Created by aleph on 2017-03-30.
 */
public enum XAReturnCode
{
    XA_RBROLLBACK(XAException.XA_RBROLLBACK),
    XA_RBCOMMFAIL(XAException.XA_RBCOMMFAIL),
    XA_RBDEADLOCK(XAException.XA_RBDEADLOCK),
    XA_RBINTEGRITY(XAException.XA_RBINTEGRITY),
    XA_RBOTHER(XAException.XA_RBOTHER),
    XA_RBPROTO(XAException.XA_RBPROTO),
    XA_RBTIMEOUT(XAException.XA_RBTIMEOUT),
    XA_RBTRANSIENT(XAException.XA_RBTRANSIENT),
    XA_RBEND(XAException.XA_RBEND),
    XA_NOMIGRATE(XAException.XA_NOMIGRATE),
    XA_HEURHAZ(XAException.XA_HEURHAZ),
    XA_HEURCOM(XAException.XA_HEURCOM),
    XA_HEURRB(XAException.XA_HEURRB),
    XA_HEURMIX(XAException.XA_HEURMIX),
    XA_RETRY(XAException.XA_RETRY),
    XA_RDONLY(XAException.XA_RDONLY),
    XA_OK(0),
    XAER_ASYNC(XAException.XAER_ASYNC),
    XAER_RMERR(XAException.XAER_RMERR),
    XAER_NOTA(XAException.XAER_NOTA),
    XAER_INVAL(XAException.XAER_INVAL),
    XAER_PROTO(XAException.XAER_PROTO),
    XAER_RMFAIL(XAException.XAER_RMFAIL),
    XAER_DUPID(XAException.XAER_DUPID),
    XAER_OUTSIDE(XAException.XAER_OUTSIDE);
    private int id;
    XAReturnCode(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static final int marshal(XAReturnCode xa)
    {
        return xa.id;
    }

    public static final XAReturnCode unmarshal(int id)
    {
        for (XAReturnCode c : XAReturnCode.values())
        {
            if (c.id == id)
            {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown XAReturnCode:" + id);
    }
}
