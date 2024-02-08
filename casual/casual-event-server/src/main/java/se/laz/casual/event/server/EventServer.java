package se.laz.casual.event.server;

import io.netty.channel.Channel;

import java.util.concurrent.ExecutorService;

public class EventServer
{
    private final Channel channel;
    private final ExecutorService executorService;

    public EventServer(Channel channel, ExecutorService executorService)
    {
        this.channel = channel;
        this.executorService = executorService;
    }




}
