/*
 * Copyright (c) 2021 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.decoding.decoders.conversation;

import se.laz.casual.network.protocol.decoding.decoders.NetworkDecoder;
import se.laz.casual.network.protocol.decoding.decoders.utils.CasualMessageDecoderUtils;
import se.laz.casual.network.protocol.decoding.decoders.utils.DecoderReaderValidator;
import se.laz.casual.network.protocol.messages.conversation.ConnectReply;
import se.laz.casual.network.protocol.messages.parseinfo.CommonSizes;
import se.laz.casual.network.protocol.messages.parseinfo.ConversationConnectReplySizes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-16.
 */
public final class ConnectReplyMessageDecoder implements NetworkDecoder<ConnectReply>
{
    private ConnectReplyMessageDecoder()
    {}

    public static NetworkDecoder<ConnectReply> of()
    {
        return new ConnectReplyMessageDecoder();
    }

    @Override
    public ConnectReply readSingleBuffer(byte[] data)
    {
        return createMessage(data);
    }

    private static ConnectReply createMessage(final byte[] data)
    {
        int currentOffset = 0;
        final UUID execution = CasualMessageDecoderUtils.getAsUUID(Arrays.copyOfRange(data, currentOffset, CommonSizes.EXECUTION.getNetworkSize()));
        currentOffset += CommonSizes.EXECUTION.getNetworkSize();

        int resultCode = ByteBuffer.wrap(data, currentOffset, ConversationConnectReplySizes.RESULT_CODE.getNetworkSize()).getInt();
        currentOffset += ConversationConnectReplySizes.RESULT_CODE.getNetworkSize();

        DecoderReaderValidator.throwIfDataNotFullyRead( currentOffset, data.length );

        return ConnectReply.createBuilder()
                .setExecution(execution)
                .setResultCode(resultCode)
                .build();
    }

}
