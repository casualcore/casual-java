/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.protocol.messages.transaction

import se.laz.casual.api.flags.Flag
import se.laz.casual.api.flags.XAFlags
import se.laz.casual.api.xa.XID
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.utils.LocalByteChannel
import spock.lang.Shared
import spock.lang.Specification

/**
 * Created by aleph on 2017-04-03.
 */
class CasualTransactionResourceRollbackRequestMessageTest extends Specification
{
    @Shared
    def execution = UUID.randomUUID()

    @Shared
    def xid = XID.NULL_XID

    @Shared
    def resourceId = 12345

    @Shared
    def flags = Flag.of().setFlag(XAFlags.TMSUCCESS)
                         .setFlag(XAFlags.TMJOIN)


    def "Message creation"()
    {
        setup:
        when:
        def msg = CasualTransactionResourceRollbackRequestMessage.of(execution, xid, resourceId, flags)
        then:
        msg.execution == execution
        msg.xid == xid
        msg.resourceId == resourceId
        msg.flags.flagValue == flags.flagValue
    }

    def "Roundtrip with message payload less than Integer.MAX_VALUE - sync"()
    {
        setup:
        def requestMsg = CasualTransactionResourceRollbackRequestMessage.of(execution, xid, resourceId, flags)
        CasualNWMessageImpl msg = CasualNWMessageImpl.of(UUID.randomUUID(), requestMsg)
        def sink = new LocalByteChannel()

        when:
        def networkBytes = msg.toNetworkBytes()
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualTransactionResourceRollbackRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink)

        then:
        networkBytes != null
        networkBytes.size() == 2
        requestMsg == resurrectedMsg.getMessage()
        msg == resurrectedMsg
    }
}
