/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.network.messages.CasualEnqueueRequest;

import java.util.Arrays;

public class CasualEnqueueRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualEnqueueRequest> matching(final CasualEnqueueRequest expected )
    {
        return new TypeSafeMatcher<CasualEnqueueRequest>()
        {
            @Override
            protected boolean matchesSafely(CasualEnqueueRequest item)
            {
                return item.getQueueName().equals(expected.getQueueName()) &&
                       Arrays.equals( item.getMessage().getPayload().toByteArray(), expected.getMessage().getPayload().toByteArray() ) &&
                       item.getMessage().getReplyQueue().equals(expected.getMessage().getReplyQueue());
            }

            @Override
            public void describeTo(Description description)
            {
            }

            @Override
            protected void describeMismatchSafely(CasualEnqueueRequest item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
