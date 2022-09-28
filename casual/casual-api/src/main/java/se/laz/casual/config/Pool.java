package se.laz.casual.config;

import java.util.Objects;

public class Pool
{
    private final String name;
    private int size;

    private Pool(String name, int size)
    {
        this.size = size;
        this.name = name;
    }

    public static Pool of(String name, int size)
    {
        Objects.requireNonNull(name, "name can not be null");
        return new Pool(name, size);
    }

    public int getSize()
   {
      return size;
   }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Pool pool = (Pool) o;
        return getSize() == pool.getSize() && Objects.equals(getName(), pool.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getSize(), getName());
    }

    @Override
    public String toString()
    {
        return "Pool{" +
                "size=" + size +
                ", name='" + name + '\'' +
                '}';
    }
}
