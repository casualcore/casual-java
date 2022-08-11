package se.laz.casual.api.queue

import spock.lang.Shared
import spock.lang.Specification

class QueueTest extends Specification
{
    @Shared
    def qname = 'CasualQueue'
    def 'with default options'()
    {
        when:
        def info = QueueInfo.of(qname)
        then:
        info.getQueueName() == qname
        info.getOptions() != null
        info.getOptions().isBlocking() == false
    }

    def 'blocking'()
    {
        when:
        def info = QueueInfo.of(qname, QueueOptions.createBuilder()
                .withBlock(true)
                .build())

        then:
        info.getQueueName() == qname
        info.getOptions() != null
        info.getOptions().isBlocking() == true
    }

}
