package se.kodarkatten.casual.test.matchers;

import org.hamcrest.TypeSafeMatcher;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.queue.CasualDequeueRequestMessage;
import se.kodarkatten.casual.network.messages.queue.CasualEnqueueRequestMessage;
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

    public static TypeSafeMatcher<CasualNWMessage<CasualEnqueueRequestMessage>> matching(final CasualEnqueueRequestMessage expected )
    {
        return CasualEnqueueRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualNWMessage<CasualDequeueRequestMessage>> matching(final CasualDequeueRequestMessage expected )
    {
        return CasualDequeueRequestMessageMatcher.matching( expected );
    }
}
