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

## Configuration

Just like casual-jca casual-caller can be configured by environment or by file.

Point to config file using env variable `CASUAL_CALLER_CONFIG_FILE`. If this file is found it will be used, otherwise, config by env will be done. Anything missing in the file will use env-values or defaults.

The following environment variables are expected

- `CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT`, String, default value "eis"
- `CASUAL_CALLER_VALIDATION_INTERVAL`, int, default value 5000
- `CASUAL_CALLER_TRANSACTION_STICKY`, boolean, default value false

A typical config file can look like the following:
```json
{
  "jndiSearchRoot": "somecustomjndiroot",
  "validationIntervalMillis": 500,
  "transactionStickyEnabled": true
}
```

## Algorithm for choosing which connection to use

If there's only one connection that matches then that one is used


If there are more than one then we randomly choose one of them to issue the actual call.

## Failover and recovery after failure

Casual caller since version 1.1.0 has built in failover for when one or more configured casual instances are unreachable. If there are multiple casual backends available attempts will be made to redirect calls to other connection factories that have discovered the same service. When performing calls services with the fewest hops will always be prioritized. If a service is known at multiple connection factories with a different number of hops the variants with more hops will only be called if the connection factory with fewer hops fails to establish connections to casual.

When casual caller detects a failure on a connection factory that factory is removed from load balancing until it has been validated that new connections can be established through it. This process runs on a timer with a default interval of 5 seconds. The interval can be configured through the environment variable `CASUAL_CALLER_VALIDATION_INTERVAL` that accepts an interval in milliseconds and should not have a unit. If invalid configuration values are detected then the timer will fall back to the default 5 second interval.

## Caching

If one or more CasualConnectionFactory isn't able to establish connections to casual upon service discovery they are skipped at that time. A record is kept of what factories have been discovered for each service to ensure that discovery can be carried out at a later time to ensure that for example load balancing keeps working even if not all configured casual backends are available at all times.

In case services are added or removed from the casual backends there is no automatic cache invalidation to handle this. Restarting the application server will always clear the cache, but casual caller also supplies a JMX bean `se.laz.casual.caller:CasualCallerControl` that allows an administrator of the application server to purge the cache for discovered services and queues respectively. The JMX bean also supplies information about what has been discovered, and can tell which CasualConnectionFactories have been checked with service discovery for each service and on which factories services are known to exist.

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

# Transaction pool sticky

Casual caller has an optional feature to sticky calls from a specific transaction to a specific casual pool. It will only sticky the first pool it sees for a given transaction and if the stickied pool is unavailable or does not serve a specific service the normal casual caller flow will be used.

Enable the feature with environment `CASUAL_CALLER_TRANSACTION_STICKY=true` or with a casual-caller config file, see configuration section.