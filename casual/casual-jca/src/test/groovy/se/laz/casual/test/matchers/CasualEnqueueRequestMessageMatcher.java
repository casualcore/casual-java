/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage;

import java.util.Arrays;

public class CasualEnqueueRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualNWMessage<CasualEnqueueRequestMessage>> matching(final CasualEnqueueRequestMessage expected )
    {
        return new TypeSafeMatcher<CasualNWMessage<CasualEnqueueRequestMessage>>()
        {
            @Override
            protected boolean matchesSafely(CasualNWMessage<CasualEnqueueRequestMessage> item)
            {
                CasualEnqueueRequestMessage msg = item.getMessage();
                return msg.getQueueName().equals(expected.getQueueName()) &&
                       Arrays.deepEquals( msg.getMessage().getPayload().getPayload().toArray(), expected.getMessage().getPayload().getPayload().toArray() ) &&
                       msg.getMessage().getReplyQueue().equals(expected.getMessage().getReplyQueue());
            }

            @Override
            public void describeTo(Description description)
            {
            }

            @Override
            protected void describeMismatchSafely(CasualNWMessage<CasualEnqueueRequestMessage> item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
