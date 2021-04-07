/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.network.messages.CasualServiceCallRequest;

import java.util.Arrays;

public final class CasualServiceCallRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualServiceCallRequest> matching(final CasualServiceCallRequest expected )
    {
        return new TypeSafeMatcher<CasualServiceCallRequest>()
        {
            @Override
            protected boolean matchesSafely(CasualServiceCallRequest item)
            {
                if( ! item.getServiceName().equals( expected.getServiceName() ) )
                {
                    return false;
                }

                byte[] itemPayload = item.getPayload().toByteArray();
                byte[] expectedPayload = expected.getPayload().toByteArray();

                if( expectedPayload.length != itemPayload.length )
                {
                    return false;
                }

                if(! Arrays.equals(expectedPayload, itemPayload))
                {
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description)
            {
            }

            @Override
            protected void describeMismatchSafely(CasualServiceCallRequest item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
