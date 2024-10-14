/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.outbound;

import io.netty.channel.epoll.EpollSocketChannel;
import se.laz.casual.config.ConfigurationOptions;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.network.ProtocolVersion;

import java.net.InetSocketAddress;

public final class NettyConnectionInformationCreator
{
    private NettyConnectionInformationCreator()
    {}

    public static NettyConnectionInformation create(InetSocketAddress address, ProtocolVersion protocolVersion)
    {

        NettyConnectionInformation.Builder builder = NettyConnectionInformation.createBuilder().withAddress(address)
                                                                  .withProtocolVersion(protocolVersion)
                                                                  .withDomainId( ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_ID ) )
                                                                  .withDomainName(ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_DOMAIN_NAME ));
        boolean useEPoll = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_OUTBOUND_USE_EPOLL );
        if(useEPoll)
        {
            builder.withChannelClass(EpollSocketChannel.class);
        }
        return builder.build();
    }
}
