package se.kodarkatten.casual.network.messages;

import org.junit.Assert;
import org.junit.Test;

public class CasualNWMessageTypeMarshalerTest
{

    @Test
    public void casualDomainDiscoveryRequestDetection()
    {
        Assert.assertEquals("verifying that marshaling and unmarshaling a messagetype returns the initial id",8001, CasualNWMessageType.marshal(CasualNWMessageType.unmarshal(8001)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void casualMessageTypeUnknownMessageTypeId()
    {
        CasualNWMessageType.unmarshal(Integer.MAX_VALUE);
    }
}
