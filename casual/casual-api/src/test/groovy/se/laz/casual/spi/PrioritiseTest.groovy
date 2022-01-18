package se.laz.casual.spi

import se.laz.casual.spi.Prioritisable
import se.laz.casual.spi.Prioritise
import se.laz.casual.spi.Priority
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class PrioritiseTest extends Specification
{
    @Shared Prioritisable first = new First()
    @Shared Prioritisable second = new Second()
    @Shared Prioritisable third = new Third()

    @Unroll
    def "Sort highest to lowest priority level."()
    {
        when:
        Prioritise.highestToLowest( input )

        then:
        input == output

        where:
        input                                   || output
        [first,second,third]                    || [first,second,third]
        [first,first,first,second,second,third] || [first,first,first,second,second,third]
        [first,first,second,first,second,third] || [first,first,first,second,second,third]
        [second,first]                          || [first,second]
        [third]                                 || [third]
        []                                      || []
    }

    @Unroll
    def "Sort lowest to highest priority level."()
    {
        when:
        Prioritise.lowestToHighest( input )

        then:
        input == output

        where:
        input                                   || output
        [first,second,third]                    || [third,second,first]
        [first,first,first,second,second,third] || [third,second,second,first,first,first]
        [first,first,second,first,second,third] || [third,second,second,first,first,first]
        [second,first]                          || [second,first]
        [third]                                 || [third]
        []                                      || []
    }


    class First implements Prioritisable
    {

        @Override
        Priority getPriority()
        {
            return Priority.LEVEL_0;
        }
    }

    class Second implements Prioritisable
    {

        @Override
        Priority getPriority()
        {
            return Priority.LEVEL_1;
        }
    }

    class Third implements Prioritisable
    {

        @Override
        Priority getPriority()
        {
            return Priority.LEVEL_2;
        }
    }



}
