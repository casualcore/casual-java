package se.laz.casual.info;

import se.laz.casual.network.messages.domain.TransactionType;

import java.util.Objects;

public class Service
{
    private final String name;
    private final String category;
    private final long hops;
    private boolean registred;
    private String jndiName;
    private TransactionType transactionType;
    private long timeout;

    private Service(Builder builder)
    {
        this.name = builder.name;
        this.category = builder.category;
        this.timeout = builder.timeout;
        this.transactionType = builder.transactionType;
        this.hops = builder.hops;
        this.registred = builder.registred;
        this.jndiName = builder.jndiName;

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

    public boolean isRegistred()
    {
        return registred;
    }

    public String getJndiName()
    {
        return jndiName;
    }

    public void setRegistred(boolean registred)
    {
        this.registred = registred;
    }

    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    public long getTimeout()
    {
        return timeout;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return hops == service.hops && registred == service.registred && Objects.equals(name, service.name) && Objects.equals(category, service.category) && Objects.equals(jndiName, service.jndiName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, category, hops, registred, jndiName, transactionType, timeout);
    }

    @Override
    public String toString()
    {
        return "Service{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", hops=" + hops +
                ", registred=" + registred +
                ", jndiName='" + jndiName + '\'' +
                ", transactionType=" + transactionType +
                ", timeout=" + timeout +
                '}';
    }

    public static class Builder {
        private String name;
        private String category;
        private long hops = 0; // 0 == inbound service
        private boolean registred = false;
        private String jndiName = "";
        private TransactionType transactionType = TransactionType.AUTOMATIC;
        private long timeout = 0;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder hops(long hops) {
            this.hops = hops;
            return this;
        }

        public Builder registred(boolean registred) {
            this.registred = registred;
            return this;
        }

        public Builder transactionType(TransactionType transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public Builder timeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder jndiName(String jndiName) {
            this.jndiName = jndiName;
            return this;
        }

        public Builder newBuilder( Service src )
        {
            return new Builder()
                    .name(src.getName())
                    .category(src.getCategory())
                    .hops(src.getHops())
                    .registred(src.isRegistred())
                    .jndiName(src.getJndiName());
        }

        public Service build() {
            return new Service(this);
        }
    }
}
