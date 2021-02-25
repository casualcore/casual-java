# Casual Caller

## What is it?

Casual caller provides an abstraction for outbound connections on top of casual-jca.


This is just a convenience layer for applications to make use, it does not in any way impact the casual-jca implementation.


## Requirements on setup of connection pools

Connection factories for outbound connection pool(s) has to exist under the same JNDI-root.

The environment variable ```CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT``` sets what the JNDI-root is.
If nothing is set then ```eis``` is used.


For wildfly/jboss the root could perhaps be set to something like ```java:/eis```.


For example, ```java:/eis/casualConnectionFactory```


## How does it work?

We look up all CasualConnectionFactories upon deployment of the casual caller application.


These entries are then cached.


Upon service or queue request, we issue a discovery request towards all those connections.
The matches are then cached.

## Algorithm for choosing which connection to use

If there's only one connection that matches then that one is used


If there are more than one then we randomly choose one of them to issue the actual call.

## How do I use casual caller in my application?

You will find casual caller as a global jndi entry, that is:
```java:/global/casual-caller-app/casual-caller/CasualCallerImpl```

This is application server agnostic.

Possible usage:
```java
public class CasualCallerProducer
{
    @Produces
    public CasualCaller get()
    {
        try
        {
            InitialContext context = new InitialContext();
            return (CasualCaller) context.lookup("java:/global/casual-caller-app/casual-caller/CasualCallerImpl");
        }
        catch (NamingException e)
        {
            throw new SomeRuntimeException(e);
        }
    }
}
```

## For applications running on wildfly

Please note that for each such application where you use the casual caller to issue calls, tpcall etc, you need a ```jboss-ejb-client.xml```

that contains the following:

```xml
<jboss-ejb-client xmlns="urn:jboss:ejb-client:1.2">
    <client-context>
        <!-- enable pass by reference -->
        <ejb-receivers local-receiver-pass-by-value="false"/>
    </client-context>
</jboss-ejb-client>
```
