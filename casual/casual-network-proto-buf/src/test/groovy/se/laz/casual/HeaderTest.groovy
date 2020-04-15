package se.laz.casual

import se.laz.casual.network.messages.Header
import spock.lang.Specification

class HeaderTest extends Specification
{
    def 'roundtrip header to file and back'()
    {
        given:
        def payloadSize = 42
        def messageType = Header.MessageType.SERVICE_CALL_REQUEST
        UUID corrid = UUID.randomUUID()
        def tmpFile = File.createTempFile('headerMessage','.bin')
        FileOutputStream os = new FileOutputStream(tmpFile)
        def header = Header.newBuilder()
                .setMessageTypeValue(messageType.number)
                .setCorridLeastSignificant(corrid.getLeastSignificantBits())
                .setCorridMostSignificant(corrid.getMostSignificantBits())
                .setPayloadSize(payloadSize)
                .build()
        header.writeTo(os)
        os.close()
        when:
        FileInputStream fs = new FileInputStream(tmpFile)
        Header restored = Header.parseFrom(fs)
        fs.close()
        then:
        restored == header
        when:
        UUID restoredUUID = new UUID(restored.corridMostSignificant, restored.corridLeastSignificant)
        then:
        restoredUUID == corrid
    }
}
