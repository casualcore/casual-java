package se.kodarkatten.casual.jca.test;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;

public final class CasualDomainDiscoveryRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualNWMessage<CasualDomainDiscoveryRequestMessage>> matching(final CasualDomainDiscoveryRequestMessage expected )
    {
        return new TypeSafeMatcher<CasualNWMessage<CasualDomainDiscoveryRequestMessage>>()
        {
            @Override
            protected boolean matchesSafely(CasualNWMessage<CasualDomainDiscoveryRequestMessage> item)
            {
                if( item.getMessage().getServiceNames().equals( expected.getServiceNames() ) )
                {
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(Description description)
            {
            }

            @Override
            protected void describeMismatchSafely(CasualNWMessage<CasualDomainDiscoveryRequestMessage> item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
