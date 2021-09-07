/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.network.protocol.messages.Routable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationDisconnectSizes;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Disconnect implements CasualNetworkTransmittable, Routable
{
    private final UUID execution;
    private final List<UUID> routes;
    private long events;

    public Disconnect(UUID execution, List<UUID> routes, long events)
    {
        this.execution = execution;
        this.routes = routes;
        this.events = events;
    }
    public UUID getExecution()
    {
        return execution;
    }

    public long getEvents()
    {
        return events;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.CONVERSATION_DISCONNECT;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final int messageSize = ConversationDisconnectSizes.EXECUTION.getNetworkSize() +
                ConversationDisconnectSizes.ROUTES_SIZE.getNetworkSize() + routes.size() * ConversationDisconnectSizes.ROUTE_ELEMENT_SIZE.getNetworkSize() +
                ConversationDisconnectSizes.EVENTS.getNetworkSize();
        return toNetworkBytes(messageSize);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Disconnect request = (Disconnect) o;
        return events == request.events && Objects.equals(execution, request.execution) && Objects.equals(routes, request.routes);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, routes, events);
    }

    @Override
    public String toString()
    {
        return "Request{" +
                "execution=" + execution +
                ", routes=" + routes +
                ", events=" + events +
                '}';
    }

    @Override
    public List<UUID> getRoutes()
    {
        return Collections.unmodifiableList(routes);
    }

    @Override
    public void setRoutes(List<UUID> routes)
    {
        this.routes.clear();
        this.routes.addAll(routes);
    }

    public static DisconnectBuilder createBuilder()
    {
        return new DisconnectBuilder();
    }

    public static final class DisconnectBuilder
    {
        private UUID execution;
        private List<UUID> routes;
        private long events;

        private DisconnectBuilder()
        {}

        public DisconnectBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public DisconnectBuilder setRoutes(List<UUID> routes)
        {
            this.routes = routes;
            return this;
        }

        public DisconnectBuilder setEvents(long events)
        {
            this.events = events;
            return this;
        }
        public Disconnect build()
        {
            return new Disconnect(execution, routes, events);
        }
    }

    private List<byte[]> toNetworkBytes(int messageSize)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(routes.size());
        routes.forEach(uuid -> CasualEncoderUtils.writeUUID(uuid, b));
        b.putLong(events);
        l.add(b.array());
        return l;
    }

}
