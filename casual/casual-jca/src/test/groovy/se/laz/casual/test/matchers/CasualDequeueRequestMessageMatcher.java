/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;

public class CasualDequeueRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualNWMessage<CasualDequeueRequestMessage>> matching(final CasualDequeueRequestMessage expected )
    {
        return new TypeSafeMatcher<CasualNWMessage<CasualDequeueRequestMessage>>()
        {
            @Override
            protected boolean matchesSafely(CasualNWMessage<CasualDequeueRequestMessage> item)
            {
                CasualDequeueRequestMessage msg = item.getMessage();
                return msg.getQueueName().equals(expected.getQueueName());
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("message with queue name " + expected.getQueueName());
            }

            @Override
            protected void describeMismatchSafely(CasualNWMessage<CasualDequeueRequestMessage> item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
