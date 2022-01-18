package se.laz.casual.spi

import se.laz.casual.spi.Priority
import spock.lang.Specification

class PriorityTest extends Specification
{
    def "GetLevel"()
    {
        expect:
        p.getLevel() == result

        where:
        p                || result
        Priority.LEVEL_0 || 0
        Priority.LEVEL_1 || 1
        Priority.LEVEL_2 || 2
        Priority.LEVEL_3 || 3
        Priority.LEVEL_4 || 4
        Priority.LEVEL_5 || 5
        Priority.LEVEL_6 || 6
        Priority.LEVEL_7 || 7
        Priority.LEVEL_8 || 8
        Priority.LEVEL_9 || 9
    }

    def "toString test"()
    {
        expect:
        Priority.LEVEL_1.toString().contains( "1" )
    }
}
