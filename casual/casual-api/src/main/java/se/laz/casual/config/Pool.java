package se.laz.casual.config;

import java.util.Objects;

public class Pool
{
   private final Address address;
   private int size;

   private Pool(Address address, int size)
   {
      this.address = address;
      this.size = size;
   }

   public static Pool of(Address address, int size)
   {
      Objects.requireNonNull(address, "address can not be null");
      return new Pool(address, size);
   }

   public Address getAddress()
   {
      return address;
   }

   public int getSize()
   {
      return size;
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
        return getSize() == pool.getSize() && Objects.equals(getAddress(), pool.getAddress());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getAddress(), getSize());
    }

    @Override
    public String toString()
    {
        return "Pool{" +
                "address=" + getAddress() +
                ", size=" + getSize() +
                '}';
    }
}
