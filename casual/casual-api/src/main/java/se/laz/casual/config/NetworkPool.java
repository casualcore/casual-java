package se.laz.casual.config;

import java.util.Objects;

public class NetworkPool
{
   private final Address address;
   private int size;

   private NetworkPool(Address address, int size)
   {
      this.address = address;
      this.size = size;
   }

   public static NetworkPool of(Address address, int size)
   {
      Objects.requireNonNull(address, "address can not be null");
      return new NetworkPool(address, size);
   }

   public Address getAddress()
   {
      return address;
   }

   public int getSize()
   {
      return size;
   }
}
