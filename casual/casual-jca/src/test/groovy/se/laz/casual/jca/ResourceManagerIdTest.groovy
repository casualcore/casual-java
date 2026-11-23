/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Specification

class ResourceManagerIdTest extends Specification
{
    def 'address-backed identifiers compare by address equality'()
    {
        given:
        ResourceManagerId first = ResourceManagerId.of(Address.of('host-a', 7771))
        ResourceManagerId same = ResourceManagerId.of(Address.of('host-a', 7771))
        ResourceManagerId different = ResourceManagerId.of(Address.of('host-b', 7771))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        first.toString().contains('host-a')
    }

    def 'domain-id-backed identifiers compare by domain id equality'()
    {
        given:
        UUID uuidA = UUID.randomUUID()
        UUID uuidB = UUID.randomUUID()
        ResourceManagerId first = ResourceManagerId.of(DomainId.of(uuidA))
        ResourceManagerId same = ResourceManagerId.of(DomainId.of(uuidA))
        ResourceManagerId different = ResourceManagerId.of(DomainId.of(uuidB))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        first.toString().contains('domainId=')
    }

    def 'address-backed and domain-id-backed identifiers are never equal'()
    {
        given:
        ResourceManagerId addressId = ResourceManagerId.of(Address.of('reverse', 0))
        ResourceManagerId domainId = ResourceManagerId.of(DomainId.of(UUID.randomUUID()))

        expect:
        addressId != domainId
        domainId != addressId
        !addressId.equals(null)
        !addressId.equals('not-an-id')
    }

    def 'of(CasualManagedConnection) selects pinned domain id for reverse outbound and address otherwise'()
    {
        given:
        DomainId domainId = DomainId.of(UUID.randomUUID())
        Address address = Address.of('host-a', 7771)
        CasualManagedConnection pinned = Mock(CasualManagedConnection) {
            getPinnedDomainId() >> Optional.of(domainId)
        }
        CasualManagedConnection unpinned = Mock(CasualManagedConnection) {
            getPinnedDomainId() >> Optional.empty()
            getAddress() >> address
        }

        expect:
        ResourceManagerId.of(pinned) == ResourceManagerId.of(domainId)
        ResourceManagerId.of(unpinned) == ResourceManagerId.of(address)
    }

    def 'null arguments are rejected'()
    {
        when:
        ResourceManagerId.of((Address) null)

        then:
        thrown(NullPointerException)

        when:
        ResourceManagerId.of((DomainId) null)

        then:
        thrown(NullPointerException)

        when:
        ResourceManagerId.of((CasualManagedConnection) null)

        then:
        thrown(NullPointerException)
    }
}
