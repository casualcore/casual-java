/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound

import jakarta.resource.spi.XATerminator
import jakarta.resource.spi.endpoint.MessageEndpointFactory
import jakarta.resource.spi.work.WorkManager
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Shared
import spock.lang.Specification

class ConnectionInformationTest extends Specification
{
    @Shared
    def mockFactory = Mock(MessageEndpointFactory)
    @Shared
    def mockXATerminator = Mock(XATerminator)
    @Shared
    def mockWorkManager = Mock(WorkManager)

    def cleanup()
    {
        ConfigurationService.reload(  )
    }

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

    def 'ok construction - no network logging'()
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
        !instance.isLogHandlerEnabled()
        !instance.isUseEpoll()
    }

    def 'ok construction - network logging'()
    {
        given:
        def instance
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER, true )

        when:
        instance = ConnectionInformation.createBuilder()
            .withWorkManager(mockWorkManager)
            .withXaTerminator(mockXATerminator)
            .withFactory(mockFactory)
            .build()


        then:
        noExceptionThrown()
        instance != null
        instance.isLogHandlerEnabled()
        !instance.isUseEpoll()
    }

    def 'ok construction - epoll enabled'()
    {
        when:
        def instance = ConnectionInformation.createBuilder()
                .withWorkManager(mockWorkManager)
                .withXaTerminator(mockXATerminator)
                .withFactory(mockFactory)
                .withUseEpoll( true )
                .build()
        then:
        noExceptionThrown()
        instance != null
        !instance.isLogHandlerEnabled()
        instance.isUseEpoll()
    }

}
