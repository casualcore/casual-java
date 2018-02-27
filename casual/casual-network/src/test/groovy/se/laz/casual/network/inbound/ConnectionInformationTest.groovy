package se.laz.casual.network.inbound

import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

class ConnectionInformationTest extends Specification
{
    @Shared
    def mockFactory = Mock(MessageEndpointFactory)
    @Shared
    def mockXATerminator = Mock(XATerminator)
    @Shared
    def mockWorkManager = Mock(WorkManager)
    def 'invalid construction'()
    {
        when:
        ConnectionInformation.createBuilder()
                             .withWorkManager(workManager)
                             .withXaTerminator(xaTerminator)
                             .withFactory(factory)
                             .build()
        then:
        thrown(NullPointerException)
        where:
        workManager      | xaTerminator      | factory
        null             | mockXATerminator  | mockFactory
        mockWorkManager  | null              | mockFactory
        mockWorkManager  | mockXATerminator  | null
    }

    def 'ok construction'()
    {
        when:
        def instance = ConnectionInformation.createBuilder()
                                            .withWorkManager(mockWorkManager)
                                            .withXaTerminator(mockXATerminator)
                                            .withFactory(mockFactory)
                                            .build()
        then:
        noExceptionThrown()
        instance != null
    }

}
