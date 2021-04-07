/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;

public final class CasualDomainDiscoveryRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualDomainDiscoveryRequest> matching(final CasualDomainDiscoveryRequest expected )
    {
        return new TypeSafeMatcher<CasualDomainDiscoveryRequest>()
        {
            @Override
            protected boolean matchesSafely(CasualDomainDiscoveryRequest item)
            {
                if( item.getServiceNamesList().equals( expected.getServiceNamesList() ) && item.getQueueNamesList().equals( expected.getQueueNamesList() ) )
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
            protected void describeMismatchSafely(CasualDomainDiscoveryRequest item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
