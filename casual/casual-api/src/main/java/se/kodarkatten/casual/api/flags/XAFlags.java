package se.kodarkatten.casual.api.flags;

import se.kodarkatten.casual.api.flags.internal.CasualFlag;

import javax.transaction.xa.XAResource;

/**
 * Created by aleph on 2017-03-30.
 */
public enum XAFlags implements CasualFlag
{
    TMNOFLAGS(XAResource.TMNOFLAGS),
    TMONEPHASE(XAResource.TMONEPHASE),
    TMFAIL(XAResource.TMFAIL),
    TMRESUME(XAResource.TMRESUME),
    TMSUCCESS(XAResource.TMSUCCESS),
    TMSUSPEND(XAResource.TMSUSPEND),
    TMSTARTRSCAN(XAResource.TMSTARTRSCAN),
    TMENDRSCAN(XAResource.TMENDRSCAN),
    TMJOIN(XAResource.TMJOIN);

    final int v;
    XAFlags(int v)
    {
        this.v = v;
    }

    @Override
    public int getValue()
    {
        return v;
    }

    public static XAFlags unmarshall( int f )
    {
        for(XAFlags v : values())
        {
            if(v.getValue() == f)
            {
                return v;
            }
        }
        throw new IllegalArgumentException("XAFlags, unknown type: " + f);
    }

}
