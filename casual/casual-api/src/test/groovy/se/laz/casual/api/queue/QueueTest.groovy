package se.laz.casual.api.queue

import spock.lang.Shared
import spock.lang.Specification

class QueueTest extends Specification
{
    @Shared
    def qspace = 'CasualQueueSpace'
    @Shared
    def qname = 'CasualQueue'
    def 'with default options'()
    {
        when:
        def info = QueueInfo.createBuilder()
                            .withQspace(qspace)
                            .withQname(qname)
                            .build()
        then:
        info.getQspace() == qspace
        info.getQname() == qname
        info.getOptions() != null
        info.getOptions().isBlocking() == false
    }

    def 'blocking'()
    {
        when:
        def info = QueueInfo.createBuilder()
                .withQspace(qspace)
                .withQname(qname)
                .withOptions(QueueOptions.createBuilder()
                                         .withBlock(true)
                                         .build())
                .build()
        then:
        info.getQspace() == qspace
        info.getQname() == qname
        info.getOptions() != null
        info.getOptions().isBlocking() == true
    }

}
