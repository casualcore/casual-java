package se.laz.casual.config;

import java.util.Objects;

public final class ReverseInbound
{
    private static int DEFAULT_SIZE = 1;
    private final Address address;
    private Integer size;

    private ReverseInbound(Address address, int size)
    {
        this.address = address;
        this.size = size;
    }

    public static ReverseInbound of(Address address, int size)
    {
        Objects.requireNonNull(address, "address can not be null");
        return new ReverseInbound(address, size);
    }

    public static ReverseInbound of(Address address)
    {
        return of(address, DEFAULT_SIZE);
    }

    public Address getAddress()
    {
        return address;
    }

    public int getSize()
    {
        return null == size ? DEFAULT_SIZE : size;
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
        ReverseInbound that = (ReverseInbound) o;
        return getSize() == that.getSize() && Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getAddress(), getSize());
    }

    @Override
    public String toString()
    {
        return "ReverseInbound{" +
                "address=" + address +
                ", size=" + size +
                '}';
    }
}
