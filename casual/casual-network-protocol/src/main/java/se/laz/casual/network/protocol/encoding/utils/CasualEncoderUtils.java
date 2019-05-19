/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.encoding.utils;

import se.laz.casual.api.xa.XIDFormatType;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.network.protocol.utils.ByteUtils;
import se.laz.casual.network.protocol.utils.XIDUtils;

import javax.transaction.xa.Xid;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-07.
 */
public final class CasualEncoderUtils
{
    private CasualEncoderUtils()
    {}

    public static ByteBuffer writeDynamicArray(ByteBuffer b, List<byte[]> data)
    {
        if(!data.isEmpty())
        {
            b.putLong(data.size());
            for(int i = 0; i < data.size(); ++i)
            {
                b.putLong(data.get(i).length);
                b.put(data.get(i));
            }
        }
        else
        {
            // need to inform that there's nothing to read
            b.putLong(0l);
        }
        return b;
    }

    public static void writeDynamicArray(List<byte[]> l, List<byte[]> data, int headerSize, int elementSize)
    {
        if(!data.isEmpty())
        {
            ByteBuffer header = ByteBuffer.allocate(headerSize);
            header.putLong(data.size());
            l.add(header.array());
            for(int i = 0; i < data.size(); ++i)
            {
                ByteBuffer elementSizeBuffer = ByteBuffer.allocate(elementSize);
                final byte[] elementData = data.get(i);
                elementSizeBuffer.putLong(elementData.length);
                l.add(elementSizeBuffer.array());
                l.add(elementData);
            }
        }
        else
        {
            // need to inform that there's nothing to read
            final ByteBuffer nothingToRead = ByteBuffer.allocate(headerSize);
            nothingToRead.putLong(0l);
            l.add(nothingToRead.array());
        }
    }

    public static ByteBuffer writeUUID(final UUID id, final ByteBuffer b)
    {
        b.putLong(id.getMostSignificantBits())
         .putLong(id.getLeastSignificantBits());
        return b;
    }

    public static ByteBuffer writeXID(final Xid xid)
    {
        final ByteBuffer xidByteBuffer = ByteBuffer.allocate(XIDUtils.getXIDNetworkSize(xid));
        return writeXID(xid, xidByteBuffer);
    }

    public static ByteBuffer writeXID(final Xid xid, final ByteBuffer b)
    {
        b.putLong(xid.getFormatId());
        if(!XIDFormatType.isNullType(xid.getFormatId()))
        {
            final byte[] gtridId = xid.getGlobalTransactionId();
            final byte[] bqual = xid.getBranchQualifier();
            b.putLong(gtridId.length)
             .putLong(bqual.length)
             .put(gtridId)
             .put(bqual);
        }
        return b;
    }

    public static byte[] writeLong(long v)
    {
        ByteBuffer b = ByteBuffer.allocate(Long.BYTES);
        b.putLong(v);
        return b.array();
    }

    public static byte[] writeInt(int v)
    {
        ByteBuffer b = ByteBuffer.allocate(Integer.BYTES);
        b.putInt(v);
        return b.array();
    }

    public static List<byte[]> writeServiceBuffer(final ServiceBuffer serviceBuffer)
    {
        List<byte[]> l = new ArrayList<>();
        List<byte[]> serviceBytes = serviceBuffer.toNetworkBytes();
        final ByteBuffer serviceBufferTypeSize = ByteBuffer.allocate(CommonSizes.SERVICE_BUFFER_TYPE_SIZE.getNetworkSize());
        serviceBufferTypeSize.putLong(serviceBytes.get(0).length);
        l.add(serviceBufferTypeSize.array());
        l.add(serviceBytes.get(0));
        serviceBytes.remove(0);
        final long payloadSize = ByteUtils.sumNumberOfBytes(serviceBytes);
        final ByteBuffer serviceBufferPayloadSizeBuffer = ByteBuffer.allocate(CommonSizes.SERVICE_BUFFER_PAYLOAD_SIZE.getNetworkSize());
        serviceBufferPayloadSizeBuffer.putLong(payloadSize);
        l.add(serviceBufferPayloadSizeBuffer.array());
        l.addAll(serviceBytes);
        return l;
    }


}
