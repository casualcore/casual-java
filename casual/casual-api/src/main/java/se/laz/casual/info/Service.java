package se.laz.casual.info;

import se.laz.casual.api.service.ServiceDetails;
import se.laz.casual.network.messages.domain.TransactionType;

import java.util.Objects;

public class Service
{
    private final String name;
    private final String category;
    private final long hops;
    private boolean registered;
    private final String jndiName;
    private final TransactionType transactionType;
    private final long timeout;
    private final Connection connection;

    private Service( Builder builder )
    {
        this.name = builder.name;
        this.category = builder.category;
        this.timeout = builder.timeout;
        this.transactionType = builder.transactionType;
        this.hops = builder.hops;
        this.registered = builder.registred;
        this.jndiName = builder.jndiName;
        this.connection = builder.connection;

        Objects.requireNonNull( name );
        Objects.requireNonNull( category );
        Objects.requireNonNull( transactionType );
        Objects.requireNonNull( jndiName );
    }

    public String getName()
    {
        return name;
    }

    public String getCategory()
    {
        return category;
    }

    public long getHops()
    {
        return hops;
    }

    public boolean isRegistered()
    {
        return registered;
    }

    public String getJndiName()
    {
        return jndiName;
    }

    public void setRegistered( boolean registered )
    {
        this.registered = registered;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public Connection getConnection()
    {
        return connection;
    }

    @Override
    public boolean equals( Object o )
    {
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }
        Service service = (Service) o;
        return hops == service.hops && registered == service.registered && timeout == service.timeout && Objects.equals( name, service.name ) && Objects.equals( category, service.category ) && Objects.equals( jndiName, service.jndiName ) && transactionType == service.transactionType && Objects.equals( connection, service.connection );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( name, category, hops, registered, jndiName, transactionType, timeout, connection );
    }

    @Override
    public String toString()
    {
        return "Service{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", hops=" + hops +
                ", registered=" + registered +
                ", jndiName='" + jndiName + '\'' +
                ", transactionType=" + transactionType +
                ", timeout=" + timeout +
                ", connection=" + connection +
                '}';
    }

    public static class Builder
    {
        private String name;
        private String category;
        private long hops = 0; // 0 == inbound service
        private boolean registred = false;
        private String jndiName = "";
        private TransactionType transactionType = TransactionType.AUTOMATIC;
        private long timeout = 0;
        private Connection connection;

        public Builder name( String name )
        {
            this.name = name;
            return this;
        }

        public Builder category( String category )
        {
            this.category = category;
            return this;
        }

        public Builder hops( long hops )
        {
            this.hops = hops;
            return this;
        }

        public Builder registred( boolean registred )
        {
            this.registred = registred;
            return this;
        }

        public Builder transactionType( TransactionType transactionType )
        {
            this.transactionType = transactionType;
            return this;
        }

        public Builder timeout( long timeout )
        {
            this.timeout = timeout;
            return this;
        }

        public Builder jndiName( String jndiName )
        {
            this.jndiName = jndiName;
            return this;
        }

        public Builder connection( Connection connection )
        {
            this.connection = connection;
            return this;
        }

        public Builder newBuilder( Service src )
        {
            return new Builder()
                    .name( src.getName() )
                    .category( src.getCategory() )
                    .hops( src.getHops() )
                    .registred( src.isRegistered() )
                    .jndiName( src.getJndiName() );
        }

        public Builder newBuilder( ServiceDetails src )
        {
            return new Builder()
                    .name( src.getName() )
                    .category( src.getCategory() )
                    .hops( src.getHops() );
        }

        public Service build()
        {
            return new Service( this );
        }
    }
}
