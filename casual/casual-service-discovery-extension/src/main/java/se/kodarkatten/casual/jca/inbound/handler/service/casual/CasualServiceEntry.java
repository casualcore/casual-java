package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A service that has been found to exist at runtime on the application server.
 */
public final class CasualServiceEntry
{
    private final String serviceName;
    private final String jndiName;
    private Method proxyMethod;

    private CasualServiceEntry( String serviceName, String jndiName, Method proxyMethod )
    {
        this.serviceName = serviceName;
        this.jndiName = jndiName;
        this.proxyMethod = proxyMethod;
    }

    public static CasualServiceEntry of( String serviceName, String jndiName, Method proxyMethod )
    {
        return new CasualServiceEntry( serviceName, jndiName, proxyMethod );
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getJndiName()
    {
        return jndiName;
    }

    public Method getProxyMethod()
    {
        return proxyMethod;
    }

    public void setProxyMethod( Method proxyMethod )
    {
        this.proxyMethod = proxyMethod;
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
        CasualServiceEntry that = (CasualServiceEntry) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(jndiName, that.jndiName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(serviceName, jndiName);
    }

    @Override
    public String toString()
    {
        return "CasualServiceEntry{" +
                "serviceName='" + serviceName + '\'' +
                ", jndiName='" + jndiName + '\'' +
                ", proxyMethod=" + proxyMethod +
                '}';
    }
}
