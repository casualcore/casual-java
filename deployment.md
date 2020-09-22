# Deployment - General
## How is Casual JCA packaged?

* Casual JCA is packaged as a Java Enterprise Archive file, e.g. `casual-jca-app-1.0.17-beta.ear`, which contains:
    * Casual Resource Adapater (RA) - casual-jca.rar
    * JCA Inbound Message Driven Bean (MDB) - casual-inbound.jar
* The JCA Inbound MDB is activated only if the Casual RA is activated
* The Casual RA is activated when the ear file is deployed only if there is the necessary configuration

Thus if there is no configuration found for the Casual RA then it is not activated and thus
the inbound part of the application does not activate correctly either.

## How is Casual RA configured?

The settings for the Casual RA include:
 
| Setting   | Comment   | Example   |
| ---   | ---   | --- |
| id | Identifier of the casual resrouce adapter must be `casual-jca` | `casual-jca` |
| archive | Location of the rar within the ear | `casual-jca-app-1.0.17-beta.ear#casual-jca.rar` |
| class-name | FQCN of the Casaul Managed Connection Factory | `se.laz.casual.jca.CasualManagedConnectionFactory` |
| HostName | Casual Server Host | `192.168.99.100` |
| PortNumber | Casual Server Port | `7771`

## Casual dependencies
In addition to the `casual-jca-app.ear` there are a number of `jar` files that need to be accessible globally.
There are a number of deployment alternatives, however the simplest is to register all of them globally i.e. to all user applications on the application server via cp configuration.

* casual-inbound-handler-api.jar - buffer and service handler registration
* casual-service-discovery-extension.jar - inbound service discovery and dispatch mechanism
* casual-api.jar - api entry point for clients
* casual-fielded-annotations.jar - fielded POJO marshalling helper

## Third party dependencies
Casual JCA makes use of the following third party libraries:

* [Netty v4.1.21.Final](https://github.com/netty/netty)
* [gson v2.8.1](https://github.com/google/gson)
* [objenis v2.6](https://github.com/easymock/objenesis)

Netty is packaged within the Casual JCA ear.

Casual api currently requires gson and objenesis, they therefore must be available from where the api is deployed.

We are working on removing dependencies to gson and objenesis in a future release.

## Fielded
If you are using fielded then you need to set the environment variable CASUAL_FIELD_TABLE to point to a json file containing all your fielded definitions.

## Dumping all network traffic to the logs
* outbound - set the environment variable CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER=true
* inbound - set the environment variable CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER=true

## Appserver specific deployment instructions
[Wildfly](deployment-wildfly.md)
