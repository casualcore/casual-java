/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.network.test.network.frombinary

import se.laz.casual.api.buffer.CasualHeaders
import se.laz.casual.api.network.protocol.messages.CasualNWMessageType
import se.laz.casual.api.network.protocol.messages.exception.CasualProtocolException
import se.laz.casual.config.ConfigurationOptions
import se.laz.casual.config.ConfigurationService
import se.laz.casual.network.ProtocolVersion
import se.laz.casual.network.protocol.decoding.CasualMessageDecoder
import se.laz.casual.network.protocol.decoding.CasualNetworkTestReader
import se.laz.casual.network.protocol.encoding.CasualMessageEncoder
import se.laz.casual.network.protocol.messages.CasualNWMessageImpl
import se.laz.casual.network.protocol.messages.parseinfo.MessageHeaderSizes
import se.laz.casual.network.protocol.messages.service.CasualServiceCallRequestMessage
import se.laz.casual.network.protocol.utils.LocalByteChannel
import se.laz.casual.network.protocol.utils.ResourceLoader
import spock.lang.Shared
import spock.lang.Specification

import java.nio.ByteBuffer

class CompleteCasualServiceCallRequestMessageTest extends Specification
{
    boolean originalStrictDataValidation

    @Shared
    String resource = '/protocol/b64/message.service.call.request.1000.3100.b64'

    @Shared
    String resourceProtocolVersion_1003 = '/protocol/b64/message.service.call.request.1003.3102.b64'

    @Shared
    String resourceProtocolVersion_1005 = '/protocol/b64/message.service.call.request.1005.3104.b64'

    @Shared
    String legacyPaddedResourceProtocolVersion_1000_1001_1002 = '/protocol/b64/message.service.call.request.1000.3100.legacy-padding.b64'

    @Shared
    String legacyPaddedResourceProtocolVersion_1003_1004 = '/protocol/b64/message.service.call.request.1003.3102.legacy-padding.b64'

    @Shared
    byte[] data

    @Shared
    byte[] dataProtocolVersion_1003

    @Shared
    byte[] dataProtocolVersion_1005

    @Shared
    byte[] legacyPaddedData_1000_1001_1002

    @Shared
    byte[] legacyPaddedDataProtocolVersion_1003_1004

    def setupSpec()
    {
        data = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resource))
        dataProtocolVersion_1003 = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersion_1003))
        dataProtocolVersion_1005 = Base64.getDecoder().decode(ResourceLoader.getResourceAsByteArray(resourceProtocolVersion_1005))
        legacyPaddedData_1000_1001_1002 = Base64.getMimeDecoder().decode(ResourceLoader.getResourceAsByteArray(legacyPaddedResourceProtocolVersion_1000_1001_1002))
        legacyPaddedDataProtocolVersion_1003_1004 = Base64.getMimeDecoder().decode(ResourceLoader.getResourceAsByteArray(legacyPaddedResourceProtocolVersion_1003_1004))
        then:
        data != null
        dataProtocolVersion_1003 != null
        dataProtocolVersion_1005 != null
        legacyPaddedData_1000_1001_1002 != null
        legacyPaddedDataProtocolVersion_1003_1004 != null
    }

    def setup()
    {
        originalStrictDataValidation = ConfigurationService.getConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION )
    }

    def "get header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(networkData, 0, MessageHeaderSizes.headerNetworkSize)
        when:
        def header = CasualMessageDecoder.networkHeaderToCasualHeader(headerData)

        then:
        header != null
        header.getType(  ) == type

        where:
        networkData              | type
        data                     | CasualNWMessageType.SERVICE_CALL_REQUEST
        dataProtocolVersion_1003 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3
        dataProtocolVersion_1005 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5
    }

    def "roundtrip header"()
    {
        setup:
        def headerData = Arrays.copyOfRange(networkData, 0, MessageHeaderSizes.headerNetworkSize)
        def header = CasualMessageDecoder.networkHeaderToCasualHeader(headerData)
        when:
        def resurrectedHeader = CasualMessageDecoder.networkHeaderToCasualHeader(header.toNetworkBytes())
        then:
        header != null
        header.getType(  ) == type
        resurrectedHeader != null
        resurrectedHeader == header

        where:
        networkData              | type
        data                     | CasualNWMessageType.SERVICE_CALL_REQUEST
        dataProtocolVersion_1003 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_3
        dataProtocolVersion_1005 | CasualNWMessageType.SERVICE_CALL_REQUEST_V_1_5
    }

    def "roundtrip message #protocolVersion"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(binary)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)
        CasualMessageEncoder.write(sink, msg)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> resurrectedMsg = CasualNetworkTestReader.read(sink, protocolVersion)
        then:
        msg != null
        msg.getMessage() == resurrectedMsg.getMessage()
        msg == resurrectedMsg
        where:
        binary                   | protocolVersion
        data                     | ProtocolVersion.VERSION_1_0
        data                     | ProtocolVersion.VERSION_1_1
        data                     | ProtocolVersion.VERSION_1_2
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_3
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_4
        dataProtocolVersion_1005 | ProtocolVersion.VERSION_1_5
    }

    def "Check message headers #protocolVersion"()
    {
        setup:
        List<byte[]> payload = new ArrayList<>()
        payload.add(binary)
        def sink = new LocalByteChannel()
        payload.each{
            bytes ->
                ByteBuffer buffer = ByteBuffer.wrap(bytes)
                sink.write(buffer)
        }
        when:
        CasualNWMessageImpl<CasualServiceCallRequestMessage> msg = CasualNetworkTestReader.read(sink, protocolVersion)

        then:
        msg.getMessage(  ).getServiceBuffer(  ).getHeaders(  ) == expectedHeaders

        where:
        binary                   | protocolVersion             | expectedHeaders
        data                     | ProtocolVersion.VERSION_1_0 | CasualHeaders.empty(  )
        data                     | ProtocolVersion.VERSION_1_1 | CasualHeaders.empty(  )
        data                     | ProtocolVersion.VERSION_1_2 | CasualHeaders.empty(  )
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_3 | CasualHeaders.empty(  )
        dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_4 | CasualHeaders.empty(  )
        dataProtocolVersion_1005 | ProtocolVersion.VERSION_1_5 | CasualHeaders.newBuilder().addAll( ["a:foo", "b:bar", "c:baz"] ).build(  )
    }

    def "decode service call request produced by a legacy sender using #protocolVersion"()
    {
        given:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, false )
        LocalByteChannel legacySource = new LocalByteChannel()
        legacySource.write(ByteBuffer.wrap(legacyNetworkData))
        LocalByteChannel currentSource = new LocalByteChannel()
        currentSource.write(ByteBuffer.wrap(currentNetworkData))

        when:
        CasualNWMessageImpl<CasualServiceCallRequestMessage> legacyMessage = CasualNetworkTestReader.read(legacySource, protocolVersion)
        CasualNWMessageImpl<CasualServiceCallRequestMessage> currentMessage = CasualNetworkTestReader.read(currentSource, protocolVersion)

        then:
        legacyMessage.message == currentMessage.message

        where:
        legacyNetworkData                         | currentNetworkData       | protocolVersion
        legacyPaddedData_1000_1001_1002           | data                     | ProtocolVersion.VERSION_1_0
        legacyPaddedData_1000_1001_1002           | data                     | ProtocolVersion.VERSION_1_1
        legacyPaddedData_1000_1001_1002           | data                     | ProtocolVersion.VERSION_1_2
        legacyPaddedDataProtocolVersion_1003_1004 | dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_3
        legacyPaddedDataProtocolVersion_1003_1004 | dataProtocolVersion_1003 | ProtocolVersion.VERSION_1_4
    }

    def "strict data validation rejects a service call request produced by a legacy sender"()
    {
        given:
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, true )
        LocalByteChannel source = new LocalByteChannel()
        source.write(ByteBuffer.wrap(legacyPaddedData_1000_1001_1002))

        when:
        CasualNetworkTestReader.read(source, ProtocolVersion.VERSION_1_0)

        then:
        thrown(CasualProtocolException)
    }

    def cleanup()
    {
        ConfigurationService.setConfiguration( ConfigurationOptions.CASUAL_NETWORK_PROTOCOL_STRICT_DATA_VALIDATION, originalStrictDataValidation )
    }
}
