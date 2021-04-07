/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.network.messages.CasualDequeueRequest;
import se.laz.casual.network.messages.CasualDomainDiscoveryRequest;
import se.laz.casual.network.messages.CasualEnqueueRequest;
import se.laz.casual.network.messages.CasualServiceCallRequest;

public final class CasualNWMessageMatchers
{

    public static TypeSafeMatcher<CasualDomainDiscoveryRequest> matching(final CasualDomainDiscoveryRequest expected )
    {
        return CasualDomainDiscoveryRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualServiceCallRequest> matching(final CasualServiceCallRequest expected )
    {
        return CasualServiceCallRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualEnqueueRequest> matching(final CasualEnqueueRequest expected )
    {
        return CasualEnqueueRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualDequeueRequest> matching(final CasualDequeueRequest expected )
    {
        return CasualDequeueRequestMessageMatcher.matching( expected );
    }
}
