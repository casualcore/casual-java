package se.laz.casual.spi;

/**
 * Priority for use when ordering handlers.
 *
 * Convension should be that 0 is highest with 9 lowest.
 */
public enum Priority
{
    LEVEL_0(0),//Highest
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5),//Casual Default
    LEVEL_6(6),
    LEVEL_7(7),
    LEVEL_8(8),
    LEVEL_9(9);//Lowest

    private final int level;

    Priority( int level )
    {
        this.level = level;
    }

    public int getLevel( )
    {
        return this.level;
    }

    @Override
    public String toString()
    {
        return "Priority{" +
                "level=" + level +
                '}';
    }
}
