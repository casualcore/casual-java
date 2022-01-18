package se.laz.casual.spi;

import java.util.Collections;
import java.util.List;

/**
 * Sort lists of objects based on their priority.
 */
public class Prioritise
{

    private Prioritise()
    {

    }

    /**
     * Order the list provided with the highest first
     * descending to the lowest.
     *
     * @param list modifiable list to order.
     */
    public static void highestToLowest(List<? extends Prioritisable> list)
    {
        Collections.sort( list );
    }

    /**
     * Order the list provided with the lowest first
     * ascending to the highest.
     *
     * @param list modifiable list to order.
     */
    public static void lowestToHighest(List<? extends Prioritisable> list)
    {
        Collections.sort( list );
        Collections.reverse( list );
    }
}
