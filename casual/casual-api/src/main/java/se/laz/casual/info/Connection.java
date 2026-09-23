package se.laz.casual.info;

import se.laz.casual.jca.DomainId;
import se.laz.casual.network.ProtocolVersion;

import java.util.Objects;

/**
 * Connection details for outbound services
 */
public class Connection
{
    private DomainId domainId;
    private ProtocolVersion protocolVersion;
    private String hostName;
    private int portNumber;

    public Connection( Builder builder )
    {
        this.domainId = builder.domainId;
        this.protocolVersion = builder.protocolVersion;
        this.hostName = builder.hostName;
        this.portNumber = builder.portNumber;

        Objects.requireNonNull( domainId );
        Objects.requireNonNull( protocolVersion );
        Objects.requireNonNull( hostName );
    }

    public DomainId getDomainId()
    {
        return domainId;
    }

    public ProtocolVersion getProtocolVersion()
    {
        return protocolVersion;
    }

    public String getHostName()
    {
        return hostName;
    }

    public int getPortNumber()
    {
        return portNumber;
    }

    @Override
    public boolean equals( Object o )
    {
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        Connection that = (Connection) o;
        return portNumber == that.portNumber && Objects.equals( domainId, that.domainId ) && protocolVersion == that.protocolVersion && Objects.equals( hostName, that.hostName );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( domainId, protocolVersion, hostName, portNumber );
    }

    @Override
    public String toString()
    {
        return "Connection{" +
                "domainId=" + domainId +
                ", protocolVersion=" + protocolVersion +
                ", hostName='" + hostName + '\'' +
                ", portNumber=" + portNumber +
                '}';
    }

    public static class Builder
    {
        DomainId domainId;
        ProtocolVersion protocolVersion;
        String hostName;
        int portNumber;
        // Do we want resourceId/connectionPoolName/connectionPoolSize?

        public Builder domainId( DomainId domainId )
        {
            this.domainId = domainId;
            return this;
        }

        public Builder protocolVersion( ProtocolVersion protocolVersion )
        {
            this.protocolVersion = protocolVersion;
            return this;
        }

        public Builder hostName( String hostName )
        {
            this.hostName = hostName;
            return this;
        }

        public Builder portNumber( int portNumber )
        {
            this.portNumber = portNumber;
            return this;
        }

        public Connection build()
        {
            return new Connection( this );
        }
    }
}
