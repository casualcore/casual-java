package se.kodarkatten.casual.connection.lookup;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Configuration implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final List<String> jndinames;
    public Configuration(final List<String> jndinames)
    {
        Objects.requireNonNull(jndinames, "jndinames can not be null");
        if(jndinames.isEmpty())
        {
            throw new CasualLookupException("no jndi names for casual instances");
        }
        this.jndinames = jndinames;
    }
    public List<String> getJNDINames()
    {
        return jndinames.stream().collect(Collectors.toList());
    }
}
