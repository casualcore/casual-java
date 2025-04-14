/*
 * Copyright (c) 2025, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProtocolMatcher
{
    private ProtocolMatcher()
    {}
    public static Long match(List<Long> protocolVersions)
    {
        Objects.requireNonNull(protocolVersions,"protocolVersions can not be null" );
        List<Long> supportedVersions = ProtocolVersion.supportedVersionNumbers();
        List<Long> matchedVersions = protocolVersions.stream().filter(supportedVersions::contains).sorted().collect(Collectors.toList());
        Collections.sort(matchedVersions);
        return matchedVersions.get(matchedVersions.size() - 1);
    }
}
