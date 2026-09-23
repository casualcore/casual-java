/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info


import se.laz.casual.jca.DomainId
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Specification

class CasualInfoTest extends Specification
{
    static final Service service = new Service.Builder()
            .name( "test" )
            .order( Order.SEQUENTIAL )
            .jndiName( "jndi" )
            .transactionType( TransactionType.ATOMIC )
            .timeout( 10 )
            .hops( 4 )
            .category( "C" )
            .build();

    void setup()
    {
        CasualInfo.clear();
    }

    def "add service"()
    {
        given:
        def domainId = DomainId.of( UUID.randomUUID() );
        Connection connection = new Connection.Builder()
                .portNumber( 9090 )
                .hostName( "host" )
                .domainId( domainId )
                .protocolVersion( ProtocolVersion.VERSION_1_0 )
                .build();
        Service outbound = new Service.Builder()
                .name( "test" )
                .transactionType( TransactionType.ATOMIC )
                .hops( 0 )
                .order(Order.CONCURRENT)
                .category( "cat" )
                .timeout( 12 )
                .connection(connection)
                .build();

        when:
        CasualInfo.addService( service )
        CasualInfo.addService( outbound )

        then:
        def serviceList = CasualInfo.getService(service.getName())
        serviceList.size() == 2
        def inb = serviceList.stream().filter { (it.order == Order.SEQUENTIAL) }.findFirst().get()
        def outb = serviceList.stream().filter { (it.order == Order.CONCURRENT) }.findFirst().get()
        inb == service
        outb.getName() == outbound.getName()
        outb.getOrder() == outbound.order
        outb.getCategory() == outbound.getCategory()
        outb.getHops() == outbound.getHops()
        !outb.isRegistered()
        outb.getJndiName().isEmpty()
        outb.getTimeout() == outbound.getTimeout()
        outb.getTransactionType() == outbound.getTransactionType()
        outb.getConnection().getDomainId() == connection.getDomainId()
        outb.getConnection().getHostName() == connection.getHostName()
        outb.getConnection().getPortNumber() == connection.getPortNumber()
        outb.getConnection().getProtocolVersion() == connection.getProtocolVersion()
    }

    def "storeEvent - no previous statistics"()
    {
        given:
        String serviceName = "test"
        char order = 'S'
        long start = 0
        long end = 10

        when:
        CasualInfo.storeEvent( serviceName, order as char, start, end )

        def event = CasualInfo.getServiceStatistic( new ServiceDescriptor( serviceName, Order.
                SEQUENTIAL ) )
        then:
        event.isPresent()
        event.get().name == serviceName
        event.get().order == order
        event.get().count == 1
        event.get().total == end
        event.get().min == end
        event.get().max == end
        event.get().last == start
    }

    def "storeEvent - updates previous statistics"()
    {
        given:
        String serviceName = "test"
        long firstStart = 10, firstEnd = 20
        CasualInfo.storeEvent( serviceName, order.getValue(), firstStart, firstEnd )

        when:
        CasualInfo.storeEvent( serviceName, order.getValue(), start, end )
        def event = CasualInfo.getServiceStatistic( new ServiceDescriptor( serviceName, order ) )

        then:
        event.isPresent()
        event.get().name == serviceName
        event.get().order == order.getValue()
        event.get().count == 2
        event.get().total == ( firstEnd - firstStart ) + ( end - start )
        event.get().min == expectedMin
        event.get().max == expectedMax
        event.get().last == start

        where:
        order | start | end | expectedMin | expectedMax
        Order.SEQUENTIAL | 10 | 20 | 10 | 10 // Call took exactly the same amount of time
        Order.SEQUENTIAL | 10 | 25 | 10 | 15 // Call took longer than first
        Order.SEQUENTIAL | 10 | 11 | 1 | 10 // Call was shorter than first
        Order.CONCURRENT | 10 | 20 | 10 | 10 // Call took exactly the same amount of time
        Order.CONCURRENT | 10 | 25 | 10 | 15 // Call took longer than first
        Order.CONCURRENT | 10 | 11 | 1 | 10 // Call was shorter than first
    }
}
