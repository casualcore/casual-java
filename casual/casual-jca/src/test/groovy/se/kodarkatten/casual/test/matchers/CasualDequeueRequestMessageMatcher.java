package se.kodarkatten.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.kodarkatten.casual.api.network.protocol.messages.CasualNWMessage;
import se.kodarkatten.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;

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
            }

            @Override
            protected void describeMismatchSafely(CasualNWMessage<CasualDequeueRequestMessage> item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
