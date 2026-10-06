/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca

import spock.lang.Specification

class DomainIdExtractorTest extends Specification
{
    def 'null is ok'()
    {
        when:
        Optional<DomainId> maybeDomainId = DomainIdExtractor.getDomainId(null)
        then:
        maybeDomainId.isEmpty()
    }

    def 'value is ok'()
    {
        given:
        DomainId domainId = DomainId.of(UUID.randomUUID())
        CasualRequestInfo requestInfo = CasualRequestInfo.of(domainId)
        when:
        Optional<DomainId> maybeDomainId = DomainIdExtractor.getDomainId(requestInfo)
        then:
        domainId == maybeDomainId.get()
    }
}
