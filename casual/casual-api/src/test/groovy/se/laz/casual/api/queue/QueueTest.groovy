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
        def info = QueueInfo.createBuilder()
                            .withQueueName(qname)
                            .build()
        then:
        info.getQueueName() == qname
        info.getOptions() != null
        info.getOptions().isBlocking() == false
    }

    def 'blocking'()
    {
        when:
        def info = QueueInfo.createBuilder()
                .withQueueName(qname)
                .withOptions(QueueOptions.createBuilder()
                        .withBlock(true)
                        .build())
                .build()
        then:
        info.getQueueName() == qname
        info.getOptions() != null
        info.getOptions().isBlocking() == true
    }

}
