/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Specification

class ResourceIdTest extends Specification
{
    def 'address-backed identifiers compare by address equality'()
    {
        given:
        ResourceId first = ResourceId.of(Address.of('host-a', 7771))
        ResourceId same = ResourceId.of(Address.of('host-a', 7771))
        ResourceId different = ResourceId.of(Address.of('host-b', 7771))

        expect:
        first == first
        first.hashCode() == first.hashCode()
        first.hashCode() != different.hashCode()
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
        ResourceId first = ResourceId.of(DomainId.of(uuidA))
        ResourceId same = ResourceId.of(DomainId.of(uuidA))
        ResourceId different = ResourceId.of(DomainId.of(uuidB))

        expect:
        first == same
        first.hashCode() == same.hashCode()
        first != different
        first.toString().contains('domainId=')
    }

    def 'address-backed and domain-id-backed identifiers are never equal'()
    {
        given:
        ResourceId addressId = ResourceId.of(Address.of('reverse', 0))
        ResourceId domainId = ResourceId.of(DomainId.of(UUID.randomUUID()))

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
        ResourceId.of(pinned) == ResourceId.of(domainId)
        ResourceId.of(unpinned) == ResourceId.of(address)
    }

    def 'null arguments are rejected'()
    {
        when:
        ResourceId.of((Address) null)

        then:
        thrown(NullPointerException)

        when:
        ResourceId.of((DomainId) null)

        then:
        thrown(NullPointerException)

        when:
        ResourceId.of((CasualManagedConnection) null)

        then:
        thrown(NullPointerException)
    }
}
