/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.Channel
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.network.ProtocolVersion
import spock.lang.Shared
import spock.lang.Specification

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable

class NettyConnectionInformationTest extends Specification
{
    @Shared
    InetSocketAddress testAddress = new InetSocketAddress(4096)
    @Shared
    UUID testDomainId = UUID.randomUUID()
    @Shared
    String testDomainName = 'nifty'
    @Shared
    long testProtocolVersion = 1000
    @Shared
    Class<? extends Channel> testChannelClass = NioSocketChannel.class
    @Shared
    Correlator testCorrelator = CorrelatorImpl.of()

    def 'failed construction'()
    {
        when:
        def instance = NettyConnectionInformation.createBuilder()
                                                 .withChannelClass(channelClass)
                                                 .withCorrelator(correlator)
                                                 .withDomainId(domainId)
                                                 .withDomainName(domainName)
                                                 .withAddress(address)
                                                 .withProtocolVersion(ProtocolVersion.unmarshall(protocolVersion))
                                                 .build()
        then:
        null == instance
        thrown(NullPointerException)
        where:
        address     | domainId       | domainName     | protocolVersion     | channelClass     | correlator
        null        | testDomainId   | testDomainName | testProtocolVersion | testChannelClass | testCorrelator
        testAddress | null           | testDomainName | testProtocolVersion | testChannelClass | testCorrelator
        testAddress | testDomainId   | null           | testProtocolVersion | testChannelClass | testCorrelator
    }

    def 'ok construction - no network logging'()
    {
        when:
        def instance = NettyConnectionInformation.createBuilder()
                .withChannelClass(channelClass)
                .withCorrelator(correlator)
                .withDomainId(domainId)
                .withDomainName(domainName)
                .withAddress(address)
                .withProtocolVersion(ProtocolVersion.unmarshall(protocolVersion))
                .build()
        then:
        null != instance
        noExceptionThrown()
        !instance.isLogHandlerEnabled()
        where:
        address     | domainId       | domainName     |  protocolVersion     | channelClass     | correlator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | testChannelClass | testCorrelator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | null             | testCorrelator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | testChannelClass | null
    }

    def 'ok construction - network logging'()
    {
        given:
        def instance

        when:
        withEnvironmentVariable(NettyConnectionInformation.USE_LOG_HANDLER_ENV_NAME,'true').execute( {
            instance = NettyConnectionInformation.createBuilder()
                    .withChannelClass(channelClass)
                    .withCorrelator(correlator)
                    .withDomainId(domainId)
                    .withDomainName(domainName)
                    .withAddress(address)
                    .withProtocolVersion(ProtocolVersion.unmarshall(protocolVersion))
                    .build()
        } )

        then:
        null != instance
        noExceptionThrown()
        instance.isLogHandlerEnabled()
        where:
        address     | domainId       | domainName     |  protocolVersion     | channelClass     | correlator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | testChannelClass | testCorrelator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | null             | testCorrelator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | testChannelClass | null
    }

}
