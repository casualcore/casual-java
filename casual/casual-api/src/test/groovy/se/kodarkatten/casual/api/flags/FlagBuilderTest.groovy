package se.kodarkatten.casual.api.flags

import spock.lang.Specification

/**
 * @author jone
 */
class FlagBuilderTest extends Specification
{
    def "test and"()
    {
        setup:
        Flag.Builder builder = new Flag.Builder(Integer.MAX_VALUE)
        when:
        builder.and(AtmiFlags.TPSIGRSTRT)
        then:
        builder.build().getFlagValue() == AtmiFlags.TPSIGRSTRT.getValue()
    }

    def "test or"()
    {
        setup:
        Flag.Builder builder = new Flag.Builder()
        when:
        builder.or(AtmiFlags.TPNOBLOCK).or(AtmiFlags.TPCONV)
        then:
        builder.build().getFlagValue() == (AtmiFlags.TPNOBLOCK.getValue() | AtmiFlags.TPCONV.getValue())
    }

    def "test and/or"()
    {
        setup:
        Flag.Builder builder = new Flag.Builder()
        when:
        builder.and(AtmiFlags.TPNOBLOCK).or(AtmiFlags.TPCONV).and(AtmiFlags.TPGETANY).or(AtmiFlags.TPNOTRAN)
        then:
        builder.build().getFlagValue() == (0&AtmiFlags.TPNOBLOCK.getValue() | AtmiFlags.TPCONV.getValue() &
                                            AtmiFlags.TPGETANY.getValue() | AtmiFlags.TPNOTRAN.getValue())
    }

}