package se.kodarkatten.casual.jca.test;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.queue.CasualEnqueueRequestMessage;

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
                       msg.getMessage().getPayload().equals(expected.getMessage().getPayload()) &&
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
