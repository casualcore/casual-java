/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.test.matchers;

import org.hamcrest.TypeSafeMatcher;
import se.laz.casual.api.network.protocol.messages.CasualNWMessage;
import se.laz.casual.network.protocol.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualDequeueRequestMessage;
import se.laz.casual.network.protocol.messages.queue.CasualEnqueueRequestMessage;
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage;

public final class CasualNWMessageMatchers
{

    public static TypeSafeMatcher<CasualNWMessage<CasualDomainDiscoveryRequestMessage>> matching(final CasualDomainDiscoveryRequestMessage expected )
    {
        return CasualDomainDiscoveryRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualNWMessage<CasualServiceCallRequestMessage>> matching(final CasualServiceCallRequestMessage expected )
    {
        return CasualServiceCallRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualNWMessage<CasualEnqueueRequestMessage>> matching(final CasualEnqueueRequestMessage expected )
    {
        return CasualEnqueueRequestMessageMatcher.matching( expected );
    }

    public static TypeSafeMatcher<CasualNWMessage<CasualDequeueRequestMessage>> matching(final CasualDequeueRequestMessage expected )
    {
        return CasualDequeueRequestMessageMatcher.matching( expected );
    }
}
