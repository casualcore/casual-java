package se.kodarkatten.casual.jca.inbound.handler.service.casual.discovery

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class JndiMatcherTest extends Specification
{


    @Shared String JNDI_SIMPLE_NO_VIEW_EJB = "java:global/casual-test-app-custom-2/test-ejb2/SimpleServiceNoViewEjb!se.kodarkatten.casual.example.service.SimpleServiceNoViewEjb"
    @Shared String JNDI_SIMPLE_SERVICE_2 = "java:global/casual-test-app-custom-2/test-ejb2/SimpleService2!se.kodarkatten.casual.example.service.ISimpleService2"
    @Shared String JNDI_CASUAL_ORDER_REMOTE = "java:global/casual-java-testapp/CasualOrderService!se.kodarkatten.casual.example.service.order.ICasualOrderServiceRemote"
    @Shared String JNDI_CASUAL_ORDER = "java:global/casual-java-testapp/CasualOrderService!se.kodarkatten.casual.example.service.order.ICasualOrderService"

    @Shared String SIMPLE_NO_VIEW_EJB_IMPL = "se.kodarkatten.casual.example.service.SimpleServiceNoViewEjb"
    @Shared String SIMPLE_SERVICE_2_INTERFACE = "se.kodarkatten.casual.example.service.ISimpleService2"
    @Shared String SIMPLE_SERVICE_2_IMPL = "se.kodarkatten.casual.example.service.SimpleService2"
    @Shared String CASUAL_ORDER_REMOTE_INTERFACE = "se.kodarkatten.casual.example.service.order.ICasualOrderServiceRemote"
    @Shared String CASUAL_ORDER_INTERFACE = "se.kodarkatten.casual.example.service.order.ICasualOrderService"
    @Shared String CASUAL_ORDER_IMPL = "se.kodarkatten.casual.example.service.order.CasualOrderService"

    @Shared List<String> jndiNames = Arrays.asList( JNDI_SIMPLE_NO_VIEW_EJB, JNDI_SIMPLE_SERVICE_2, JNDI_CASUAL_ORDER, JNDI_CASUAL_ORDER_REMOTE )

    @Unroll
    def "Match entry to a list of possible jndi values."()
    {
        when:
        String found = JndiMatcher.findMatch( implementationType, ejbName, interfaceType, jndiNames )

        then:
        found == result

        where:
        implementationType      | ejbName | interfaceType                 | result
        CASUAL_ORDER_IMPL       | null    | CASUAL_ORDER_REMOTE_INTERFACE | JNDI_CASUAL_ORDER_REMOTE
        CASUAL_ORDER_IMPL       | null    | CASUAL_ORDER_INTERFACE        | JNDI_CASUAL_ORDER
        CASUAL_ORDER_IMPL       | null    | SIMPLE_SERVICE_2_INTERFACE    | null
        SIMPLE_SERVICE_2_IMPL   | null    | SIMPLE_SERVICE_2_INTERFACE    | JNDI_SIMPLE_SERVICE_2
        SIMPLE_NO_VIEW_EJB_IMPL | null    | null                          | JNDI_SIMPLE_NO_VIEW_EJB
    }

}
