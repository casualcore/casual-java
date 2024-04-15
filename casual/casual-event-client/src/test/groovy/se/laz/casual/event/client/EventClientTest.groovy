package se.laz.casual.event.client

import spock.lang.Shared
import spock.lang.Specification

class EventClientTest extends Specification
{
    @Shared
    ConnectionInformation ci = new ConnectionInformation("localhost", 12345)
    @Shared
    EventObserver nopEventObserver = {}
    @Shared
    ConnectionObserver nopConnectionObserver = {}
    @Shared
    InitFunction nopInitFunction = {}
    def 'failed construction'()
    {
        when:
        EventClient.of(connectionInformation, eventObserver, connectionObserver, initFunction, enableLogging)
        then:
        thrown(NullPointerException)
        where:
        connectionInformation     || eventObserver       || connectionObserver       || initFunction    || enableLogging
        null                      || nopEventObserver    || nopConnectionObserver    || nopInitFunction || true
        ci                        || null                || nopConnectionObserver    || nopInitFunction || true
        ci                        || nopEventObserver    || null                     || nopInitFunction || true
        ci                        || nopEventObserver    || nopConnectionObserver    || null            || true
    }
    


}
