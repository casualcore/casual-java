/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.conversation;

import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.conversation.Duplex;
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType;
import se.laz.casual.api.network.protocol.messages.CasualNetworkTransmittable;
import se.laz.casual.network.protocol.encoding.utils.CasualEncoderUtils;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationRequestSizes;
import se.laz.casual.network.protocol.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Request implements CasualNetworkTransmittable
{
    private final UUID execution;
    private final Duplex duplex;
    private int resultCode;
    private long userCode;
    private final ServiceBuffer serviceBuffer;

    public Request(UUID execution, Duplex duplex, int resultCode, long userCode, ServiceBuffer serviceBuffer)
    {
        this.execution = execution;
        this.duplex = duplex;
        this.resultCode = resultCode;
        this.userCode = userCode;
        this.serviceBuffer = serviceBuffer;
    }

    public UUID getExecution()
    {
        return execution;
    }

    public Duplex getDuplex()
    {
        return duplex;
    }

    public int getResultCode()
    {
        return resultCode;
    }

    public long getUserCode()
    {
        return userCode;
    }

    public ServiceBuffer getServiceBuffer()
    {
        return serviceBuffer;
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
                ConversationRequestSizes.DUPLEX.getNetworkSize() +
                ConversationRequestSizes.RESULT_CODE.getNetworkSize() +
                ConversationRequestSizes.USER_CODE.getNetworkSize() +
                ConversationRequestSizes.BUFFER_TYPE_NAME_SIZE.getNetworkSize() + ConversationRequestSizes.BUFFER_PAYLOAD_SIZE.getNetworkSize() + ByteUtils.sumNumberOfBytes(serviceBytes);
        return toNetworkBytes((int)messageSize, serviceBytes);
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
        return resultCode == request.resultCode && userCode == request.userCode && Objects.equals(execution, request.execution) && duplex == request.duplex;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(execution, duplex, resultCode, userCode);
    }

    @Override
    public String toString()
    {
        return "Request{" +
                "execution=" + execution +
                ", duplex=" + duplex +
                ", resultCode=" + resultCode +
                ", userCode=" + userCode +
                ", serviceBuffer=" + serviceBuffer +
                '}';
    }

    public static RequestBuilder createBuilder()
    {
        return new RequestBuilder();
    }

    public static final class RequestBuilder
    {
        private static final int NO_RESULT_CODE = -1;
        private UUID execution;
        private Duplex duplex;
        private int resultCode = NO_RESULT_CODE;
        private long userCode;
        private ServiceBuffer serviceBuffer;

        private RequestBuilder()
        {}

        public RequestBuilder setExecution(UUID execution)
        {
            this.execution = execution;
            return this;
        }

        public RequestBuilder setDuplex(Duplex duplex)
        {
            this.duplex = duplex;
            return this;
        }

        public RequestBuilder setResultCode(int resultCode)
        {
            this.resultCode = resultCode;
            return this;
        }

        public RequestBuilder setUserCode(long userCode)
        {
            this.userCode = userCode;
            return this;
        }

        public RequestBuilder setServiceBuffer(ServiceBuffer serviceBuffer)
        {
            this.serviceBuffer = serviceBuffer;
            return this;
        }

        public Request build()
        {
            Objects.requireNonNull(execution, "execution can not be null");
            Objects.requireNonNull(serviceBuffer, "serviceBuffer can not be null");
            Objects.requireNonNull(duplex, "duplex can not be null");
            Objects.requireNonNull(serviceBuffer, "serviceBuffer can not be null");
            return new Request(execution, duplex, resultCode, userCode, serviceBuffer);
        }
    }

    private List<byte[]> toNetworkBytes(int messageSize, List<byte[]> serviceBytes)
    {
        List<byte[]> l = new ArrayList<>();
        ByteBuffer b = ByteBuffer.allocate(messageSize);
        CasualEncoderUtils.writeUUID(execution, b);
        b.putShort(duplex.getValue())
         .putInt(resultCode)
         .putLong(userCode);
        b.putLong(serviceBytes.get(0).length).put(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        b.putLong(payloadSize);
        serviceBytes.forEach(b::put);
        l.add(b.array());
        return l;
    }

}
