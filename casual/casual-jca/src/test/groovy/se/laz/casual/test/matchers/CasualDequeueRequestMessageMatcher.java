/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.network.messages.CasualDequeueRequest;

public class CasualDequeueRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualDequeueRequest> matching(final CasualDequeueRequest expected )
    {
        return new TypeSafeMatcher<CasualDequeueRequest>()
        {
            @Override
            protected boolean matchesSafely(CasualDequeueRequest item)
            {
                return item.getQueueName().equals(expected.getQueueName());
            }

            @Override
            public void describeTo(Description description)
            {
            }

            @Override
            protected void describeMismatchSafely(CasualDequeueRequest item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
