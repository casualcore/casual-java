/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

import java.util.Arrays;
import java.util.List;

public final class CasualServiceCallRequestMessageMatcher
{
    public static TypeSafeMatcher<CasualNWMessage<CasualServiceCallRequestMessage>> matching(final CasualServiceCallRequestMessage expected )
    {
        return new TypeSafeMatcher<CasualNWMessage<CasualServiceCallRequestMessage>>()
        {
            @Override
            protected boolean matchesSafely(CasualNWMessage<CasualServiceCallRequestMessage> item)
            {
                CasualServiceCallRequestMessage msg = item.getMessage();

                if( ! msg.getServiceName().equals( expected.getServiceName() ) )
                {
                    return false;
                }

                List<byte[]> itemPayload = msg.getServiceBuffer().getPayload();
                List<byte[]> expectedPayload = expected.getServiceBuffer().getPayload();

                if( expectedPayload.size() != itemPayload.size() )
                {
                    return false;
                }

                for( int i=0; i< expectedPayload.size(); i++ )
                {
                    if( ! Arrays.equals( expectedPayload.get( i ), itemPayload.get( i ) ) )
                    {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void describeTo(Description description)
            {
            }

            @Override
            protected void describeMismatchSafely(CasualNWMessage<CasualServiceCallRequestMessage> item, Description mismatchDescription)
            {
                super.describeMismatchSafely(item, mismatchDescription);
            }
        };
    }
}
