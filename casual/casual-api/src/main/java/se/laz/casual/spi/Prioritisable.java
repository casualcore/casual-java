package se.laz.casual.spi;

public interface Prioritisable extends Comparable<Prioritisable>
{
    /**
     * The priority of the object.
     *
     * @return priority of the object.
     */
    default Priority getPriority()
    {
        return Priority.LEVEL_5;
    }

    default int compareTo(Prioritisable o)
    {
        return this.getPriority().getLevel() - o.getPriority().getLevel();
    }
}
