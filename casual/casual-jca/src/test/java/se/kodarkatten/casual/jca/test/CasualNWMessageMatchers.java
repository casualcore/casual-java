package se.kodarkatten.casual.jca.test;

import org.hamcrest.TypeSafeMatcher;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;

public final class CasualNWMessageMatchers
{

    public static TypeSafeMatcher<CasualNWMessage<CasualDomainDiscoveryRequestMessage>> matching(final CasualDomainDiscoveryRequestMessage expected )
    {
        return CasualDomainDiscoveryRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualNWMessage<CasualServiceCallRequestMessage>> matching(final CasualServiceCallRequestMessage expected )
    {
        return CasualServiceCallRequestMessageMatcher.matching( expected );
    }
}
