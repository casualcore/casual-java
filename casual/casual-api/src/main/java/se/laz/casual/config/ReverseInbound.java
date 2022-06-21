package se.laz.casual.config;

import java.util.Objects;

public final class ReverseInbound
{
    private final Address address;

    private ReverseInbound(Address address)
    {
        this.address = address;
    }

    public static ReverseInbound of(Address address)
    {
        Objects.requireNonNull(address, "address can not be null");
        return new ReverseInbound(address);
    }

    public Address getAddress()
    {
        return address;
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
        return Objects.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getAddress());
    }

    @Override
    public String toString()
    {
        return "ReverseInbound{" +
                "address=" + address +
                '}';
    }
}
