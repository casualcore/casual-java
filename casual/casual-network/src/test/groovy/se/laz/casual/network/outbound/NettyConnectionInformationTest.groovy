/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound

import io.netty.channel.Channel
import io.netty.channel.socket.nio.NioSocketChannel
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
    long testProtocolVersion = 1
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
                                                 .withProtocolVersion(protocolVersion)
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

    def 'ok construction'()
    {
        when:
        def instance = NettyConnectionInformation.createBuilder()
                .withChannelClass(channelClass)
                .withCorrelator(correlator)
                .withDomainId(domainId)
                .withDomainName(domainName)
                .withAddress(address)
                .withProtocolVersion(protocolVersion)
                .build()
        then:
        null != instance
        noExceptionThrown()
        where:
        address     | domainId       | domainName     |  protocolVersion     | channelClass     | correlator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | testChannelClass | testCorrelator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | null             | testCorrelator
        testAddress | testDomainId   | testDomainName |  testProtocolVersion | testChannelClass | null
    }

}
