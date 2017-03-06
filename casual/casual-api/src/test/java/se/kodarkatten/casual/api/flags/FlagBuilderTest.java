package se.kodarkatten.casual.api.flags;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author jone
 */
public class FlagBuilderTest
{
    @Test
    public void andTest() throws Exception
    {
        Flag.Builder builder = new Flag.Builder(Integer.MAX_VALUE);

        builder.and(AtmiFlags.TPSIGRSTRT);

        assertEquals("Checking that two values bitwise ands together correctly",
                     AtmiFlags.TPSIGRSTRT.getValue(), builder.build().getFlagValue());
    }

    @Test
    public void orTest() throws Exception
    {
        Flag.Builder builder = new Flag.Builder();

        builder.or(AtmiFlags.TPNOBLOCK).or(AtmiFlags.TPCONV);


        assertEquals("Checking that two values bitwise ors together correctly",
                     AtmiFlags.TPNOBLOCK.getValue() | AtmiFlags.TPCONV.getValue(), builder.build().getFlagValue());
    }

    @Test
    public void andOrandOrTest()
    {
        Flag.Builder builder = new Flag.Builder();

        builder.and(AtmiFlags.TPNOBLOCK).or(AtmiFlags.TPCONV).and(AtmiFlags.TPGETANY).or(AtmiFlags.TPNOTRAN);


        assertEquals("Checking that a series values and and ors together correctly",
                     0&AtmiFlags.TPNOBLOCK.getValue() | AtmiFlags.TPCONV.getValue() &
                             AtmiFlags.TPGETANY.getValue() | AtmiFlags.TPNOTRAN.getValue(), builder.build().getFlagValue());
    }

}