/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.inbound


import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

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

        when:
        withEnvironmentVariable(ConnectionInformation.USE_LOG_HANDLER_ENV_NAME,'true').execute(
                {
                    instance = ConnectionInformation.createBuilder()
                        .withWorkManager(mockWorkManager)
                        .withXaTerminator(mockXATerminator)
                        .withFactory(mockFactory)
                        .build()
                }
        )

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
