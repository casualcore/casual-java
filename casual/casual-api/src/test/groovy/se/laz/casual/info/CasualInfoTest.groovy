/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.info


import se.laz.casual.api.service.ServiceDetails
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

    def "add inbound service"()
    {
        when:
        CasualInfo.getInstance().addService( service )

        then:
        CasualInfoStorage.getInstance().getService( new ServiceDescriptor( service.getName(), Order.SEQUENTIAL ) ).
                isPresent()
        CasualInfoStorage.getInstance().getService( new ServiceDescriptor( service.getName(), Order.SEQUENTIAL ) ).
                get() == service
    }

    def "add discovery"()
    {
        given:
        def domainId = DomainId.of( UUID.randomUUID() );
        Connection connection = new Connection.Builder()
                .portNumber( 9090 )
                .hostName( "host" )
                .domainId( domainId )
                .protocolVersion( ProtocolVersion.VERSION_1_0 )
                .build();
        ServiceDetails serviceDetails = ServiceDetails.createBuilder()
                .withName( "test" )
                .withTransactionType( TransactionType.ATOMIC )
                .withHops( 0 )
                .withCategory( "cat" )
                .withTimeout( 12 )
                .build();

        when:
        CasualInfo.getInstance().addDiscovery( List.of( serviceDetails ), connection )

        then:
        def present = CasualInfoStorage.getInstance()
                .getService( new ServiceDescriptor( serviceDetails.name, Order.CONCURRENT ) )
        present.isPresent()
        present.get().getName() == serviceDetails.getName()
        present.get().getOrder() == Order.CONCURRENT
        present.get().getCategory() == serviceDetails.getCategory()
        present.get().getHops() == serviceDetails.getHops()
        !present.get().isRegistered()
        present.get().getJndiName().isEmpty()
        present.get().getTimeout() == serviceDetails.getTimeout()
        present.get().getTransactionType() == serviceDetails.getTransactionType()
        present.get().getConnection().getDomainId() == connection.getDomainId()
        present.get().getConnection().getHostName() == connection.getHostName()
        present.get().getConnection().getPortNumber() == connection.getPortNumber()
        present.get().getConnection().getProtocolVersion() == connection.getProtocolVersion()
    }

    def "storeEvent - no previous statistics"()
    {
        given:
        String serviceName = "test"
        char order = 'S'
        long start = 0
        long end = 10

        when:
        CasualInfo.getInstance().storeEvent( serviceName, order as char, start, end )

        def event = CasualInfoStorage.getInstance().getServiceStatistic( new ServiceDescriptor( serviceName, Order.
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
        CasualInfoStorage.getInstance().putEvent( new ServiceDescriptor( prevEvent.name, order ), prevEvent )

        when:
        CasualInfo.getInstance().storeEvent( prevEvent.getName(), prevEvent.getOrder() as char, start, end )
        def event = CasualInfoStorage.getInstance().getServiceStatistic( new ServiceDescriptor( prevEvent.getName(),
                order ) )

        then:
        event.isPresent()
        event.get().name == prevEvent.getName()
        event.get().order == prevEvent.getOrder()
        event.get().count == prevEvent.getCount() + 1
        event.get().total == prevEvent.getTotal() + ( end - start )
        event.get().min == expectedMin
        event.get().max == expectedMax
        event.get().last == start

        where:
        prevEvent                                        | order            | start | end | expectedMin | expectedMax
        getEventBuilder( 'S' as char ).build()           | Order.SEQUENTIAL | 10    | 20  | 10          | 20
        getEventBuilder( 'S' as char ).min( 1 ).build()  | Order.SEQUENTIAL | 10    | 11  | 1           | 20
        getEventBuilder( 'S' as char ).max( 10 ).build() | Order.SEQUENTIAL | 10    | 100 | 10          | 90
        getEventBuilder( 'C' as char ).build()           | Order.CONCURRENT | 10    | 20  | 10          | 20
        getEventBuilder( 'C' as char ).min( 1 ).build()  | Order.CONCURRENT | 10    | 11  | 1           | 20
        getEventBuilder( 'C' as char ).max( 10 ).build() | Order.CONCURRENT | 10    | 100 | 10          | 90
    }

    def getEventBuilder( char order )
    {
        return new EventServiceStatistics.Builder()
                .name( "test" ).order( order ).min( 10 ).max( 20 );
    }
}
