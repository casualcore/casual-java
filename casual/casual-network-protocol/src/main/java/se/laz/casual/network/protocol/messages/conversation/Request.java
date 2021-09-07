/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.api.network.protocol.messages.Routable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Request implements CasualNetworkTransmittable, Routable
{
    private final UUID execution;
    private final List<UUID> routes;
    private long events;
    private int resultCode;
    private final ServiceBuffer serviceBuffer;

    public Request(UUID execution, List<UUID> routes, long events, int resultCode, ServiceBuffer serviceBuffer)
    {
        this.execution = execution;
        this.routes = routes;
        this.events = events;
        this.resultCode = resultCode;
        this.serviceBuffer = serviceBuffer;
    }

    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public long getEvents()
    {
        return events;
    }

    public int getResultCode()
    {
        return resultCode;
    }

    @Override
    public CasualNWMessageType getType()
    {
        return CasualNWMessageType.CONVERSATION_REQUEST;
    }

    @Override
    public List<byte[]> toNetworkBytes()
    {
        final List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();
        final long messageSize = ConversationRequestSizes.EXECUTION.getNetworkSize() +
                ConversationRequestSizes.ROUTES_SIZE.getNetworkSize() + routes.size() * ConversationRequestSizes.ROUTE_ELEMENT_SIZE.getNetworkSize() +
                ConversationRequestSizes.EVENTS.getNetworkSize() +
                ConversationRequestSizes.RESULT_CODE.getNetworkSize() +
                ConversationRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ConversationRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        return toNetworkBytes((int)messageSize, serviceBytes);
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
        Request request = (Request) o;
        return events == request.events && resultCode == request.resultCode && Objects.equals(execution, request.execution) && Objects.equals(routes, request.routes);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, routes, events, resultCode);
    }

    @Override
    public String toString()
    {
        return "Request{" +
                "execution=" + execution +
                ", routes=" + routes +
                ", events=" + events +
                ", resultCode=" + resultCode +
                ", serviceBuffer=" + serviceBuffer +
                '}';
    }

    public static RequestBuilder createBuilder()
    {
        return new RequestBuilder();
    }

    public static final class RequestBuilder
    {
        private UUID execution;
        private List<UUID> routes;
        private long events;
        private int resultCode;
        private ServiceBuffer serviceBuffer;

        private RequestBuilder()
        {}

        public RequestBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public RequestBuilder setRoutes(List<UUID> routes)
        {
            this.routes = routes;
            return this;
        }

        public RequestBuilder setEvents(long events)
        {
            this.events = events;
            return this;
        }

        public RequestBuilder setResultCode(int resultCode)
        {
            this.resultCode = resultCode;
            return this;
        }

        public RequestBuilder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }

        public Request build()
        {
            return new Request(execution, routes, events, resultCode, serviceBuffer);
        }
    }

    private List<byte[]> toNetworkBytes(int messageSize, List<byte[]> serviceBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putLong(routes.size());
        routes.forEach(uuid -> CasualEncoderUtils.writeUUID(uuid, b));
        b.putLong(events).putInt(resultCode);
        b.putLong(serviceBytes.get(0).length).put(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        b.putLong(payloadSize);
        serviceBytes.forEach(b::put);
        l.add(b.array());
        return l;
    }

}
