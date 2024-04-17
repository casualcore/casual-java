/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.server;

import io.netty.channel.Channel;
import se.laz.casual.event.server.messages.ConnectReply;
import se.laz.casual.event.server.messages.ConnectReplyMessage;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionReplier
{
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();
    private static final ConnectReplyMessage REPLY_MSG = ConnectReplyMessage.of(ConnectReply.CONNECT_REPLY);
    private ConnectionReplier()
    {}
    public static ConnectionReplier of()
    {
        return new ConnectionReplier();
    }
    public void clientConnected(Channel channel)
    {
        Objects.requireNonNull(channel, "channel can not be null");
        EXECUTOR_SERVICE.submit(() -> channel.writeAndFlush(REPLY_MSG));
    }
}
