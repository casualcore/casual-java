/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;

public final class CasualDomainDiscoveryRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualNWMessage<CasualDomainDiscoveryRequestMessage>> matching(final CasualDomainDiscoveryRequestMessage expected )
    {
        return new TypeSafeMatcher<CasualNWMessage<CasualDomainDiscoveryRequestMessage>>()
        {
            @Override
            protected boolean matchesSafely(CasualNWMessage<CasualDomainDiscoveryRequestMessage> item)
            {
                if( item.getMessage().getServiceNames().equals( expected.getServiceNames() ) && item.getMessage().getQueueNames().equals( expected.getQueueNames() ) )
                {
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("servicenames and queuenames were not matching");
            }

            @Override
            protected void describeMismatchSafely(CasualNWMessage<CasualDomainDiscoveryRequestMessage> item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
