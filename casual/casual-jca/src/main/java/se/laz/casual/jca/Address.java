package se.laz.casual.jca;

import java.util.Objects;

public class Address
{
    private final String hostName;
    private final Integer port;

    private Address(String hostName, Integer port)
    {
        this.hostName = hostName;
        this.port = port;
    }

    public static Address of(String hostName, Integer portNumber)
    {
        Objects.requireNonNull(hostName, "hostName can not be null");
        Objects.requireNonNull(portNumber, "portNumber can not be null");
        return new Address(hostName,portNumber);
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
        Address address = (Address) o;
        return hostName.equals(address.hostName) && port.equals(address.port);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(hostName, port);
    }

    @Override
    public String toString()
    {
        return "Address{" +
                "host='" + hostName + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
