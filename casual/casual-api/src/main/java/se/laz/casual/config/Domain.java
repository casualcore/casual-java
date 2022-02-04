package se.laz.casual.config;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Holds the fixed values for id and name for the whole domain.
 *
 * At startup a random UUID is selected for id and name is set from environment variable {@value #DOMAIN_NAME_ENV}, or is
 * set to an empty string ({@value #DOMAIN_NAME_DEFAULT}) if the environment variable isn't set.
 */
public final class Domain
{
    public static final String DOMAIN_NAME_ENV = "CASUAL_DOMAIN_NAME";
    public static final String DOMAIN_NAME_DEFAULT = "";

    private static final UUID id = UUID.randomUUID();
    private final String name;

    /**
     * Primarily here to make sure gson-deserialized instances always get a good default value, even if a Domain
     * is configured by json file, but fails to set the name property.
     */
    private Domain()
    {
        name = getFromEnvIfAvailableDefaultIfNot();
    }


    private Domain(String domainName)
    {
        this.name = Optional.ofNullable(domainName).orElse(getFromEnvIfAvailableDefaultIfNot());
    }

    private String getFromEnvIfAvailableDefaultIfNot()
    {
        return Optional.ofNullable(System.getenv(DOMAIN_NAME_ENV)).orElse(DOMAIN_NAME_DEFAULT);
    }

    /**
     * Get instance with value from external config.
     * @param domainName The name to set for the domain.
     * @return New Domain instance with name set to the supplied domainName and with the static id that is randomly
     * assigned for this domain at startup.
     */
    protected static Domain of(String domainName)
    {
        return new Domain(domainName);
    }

    /**
     * Standard way to get instance by environment.
     * @return New Domain instance with name set dependeing on environment and with the static id that is randomly
     * assigned for this domain at startup.
     */
    protected static Domain getFromEnv()
    {
        return new Domain(System.getenv(DOMAIN_NAME_ENV));
    }

    public UUID getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        Domain domain = (Domain) o;
        return getId().equals(domain.getId()) && getName().equals(domain.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString()
    {
        return "Domain{id="+id+", name="+name+"}";
    }
}
