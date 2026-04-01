/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.Channel
import io.netty.channel.socket.nio.NioSocketChannel
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import spock.lang.Shared
import spock.lang.Specification

class NettyConnectionInformationTest extends Specification
{
    @Shared
    InetSocketAddress testAddress = new InetSocketAddress(4096)
    @Shared
    UUID testDomainId = UUID.randomUUID()
    @Shared
    String testDomainName = 'nifty'
    @Shared
    Class<? extends Channel> testChannelClass = NioSocketChannel.class
    @Shared
    Correlator testCorrelator = CorrelatorImpl.of()

    def cleanup()
    {
        ConfigurationService.reload(  )
    }

    def 'failed construction'()
    {
        when:
        def instance = NettyConnectionInformation.createBuilder()
                                                 .withChannelClass(channelClass)
                                                 .withCorrelator(correlator)
                                                 .withDomainId(domainId)
                                                 .withDomainName(domainName)
                                                 .withAddress(address)
                                                 .build()
        then:
        null == instance
        thrown(NullPointerException)
        where:
        address     | domainId       | domainName     |  channelClass     | correlator
        null        | testDomainId   | testDomainName |  testChannelClass | testCorrelator
        testAddress | null           | testDomainName |  testChannelClass | testCorrelator
        testAddress | testDomainId   | null           |  testChannelClass | testCorrelator
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
                .build()
        then:
        null != instance
        noExceptionThrown()
        !instance.isLogHandlerEnabled()
        where:
        address     | domainId       | domainName     |  channelClass     | correlator
        testAddress | testDomainId   | testDomainName |  testChannelClass | testCorrelator
        testAddress | testDomainId   | testDomainName |  null             | testCorrelator
        testAddress | testDomainId   | testDomainName |  testChannelClass | null
    }

    def 'ok construction - network logging'()
    {
        given:
        def instance
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER, true )

        when:
        instance = NettyConnectionInformation.createBuilder()
                .withChannelClass(channelClass)
                .withCorrelator(correlator)
                .withDomainId(domainId)
                .withDomainName(domainName)
                .withAddress(address)
                .build()

        then:
        null != instance
        noExceptionThrown()
        instance.isLogHandlerEnabled()

        where:
        address     | domainId       | domainName     |  channelClass     | correlator
        testAddress | testDomainId   | testDomainName |  testChannelClass | testCorrelator
        testAddress | testDomainId   | testDomainName |  null             | testCorrelator
        testAddress | testDomainId   | testDomainName |  testChannelClass | null
    }

}
