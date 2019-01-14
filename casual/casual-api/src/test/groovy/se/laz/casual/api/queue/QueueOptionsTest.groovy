package se.laz.casual.api.queue

import spock.lang.Specification

class QueueOptionsTest extends Specification
{
    def 'default options'()
    {
        when:
        def options = QueueOptions.defaultOptions()
        then:
        options.isBlocking() == false
    }

    def 'set blocking'()
    {
        when:
        def options = QueueOptions.createBuilder()
                                  .withBlock(true)
                                  .build()
        then:
        options.isBlocking() == true
    }
}
