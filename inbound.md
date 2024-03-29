# Casual Inbound Server

The inbound server is an implementation of the JCA Inbound specification, using an embedded netty server 
to serve incoming requests.

The inbound server receives service requests from casual outbound clients, forwards those requests 
onto the registered services and returns the results. Where applicable these requests are run within the 
context of an XA transaction.

## Inbound Services

There are currently two types of services which are supported by Casual JCA.
* Casual Services
* JavaEE Services - (testing purpose only)

### Casual Service Registration

In order to register inbound services, applications can for example use the `@CasualService` annotation upon their
method of a `@Remote` Java bean.
In the following example you can see how a service called `echo` is defined.

```java
@Stateless
@Remote(EchoService.class)
public class EchoServiceImpl implements EchoService
{

    @CasualService(name="echo", category = "test" )
    @Override
    public InboundResponse echo(InboundRequest buffer)
    {
        return InboundResponse.createBuilder()
                .buffer( buffer.getBuffer() )
                .build();
    }
}
``` 
During application deployment Casual JCA application will `@Observe` the deployment of the `@CasualService` and 
register them as inbound services, allowing clients to make requests against these services.
This applies to all applications that are deployed throughout the runtime of the application server.

The resulting inbound service registry is used to respond to domain discovery requests. As well as to dispatch incoming
request to the appropriate deployed application. As new applications containing casual inbound services are deployed, 
the domain discovery requests will respond accordingly.

NB - undeployment of an application currently does not remove the associated casual services from the registry.

### Extending Casual Service Handler

If you want to use the service handler that is provided, but the default implementation does not suit your needs - you can then
create an SPI extension that implements *ServiceHandlerExtension* with a priority lower than default *Priority.LEVEL_5*.

See [ServiceHandlerExtension](casual/casual-inbound-handler-api/src/main/java/se/laz/casual/jca/inbound/handler/service/extension/ServiceHandlerExtension.java)

ServiceHandlers retrieve registered ServiceHandlerExtension and filters them using the canHandle method.

ServiceHandlers filter by providing the name of, for example, the annotation they are associated with.

Therefore to add an extension for CasualServices, which are normally handled within CasualServiceHandler, you would
implement the canHandle method as follow:

```java
package mypackage;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.jca.inbound.handler.service.extension.ServiceHandlerExtension;

public class MyServiceHandlerExtension implements ServiceHandlerExtension
{
    @Override
    public boolean canHandle( String name )
    {
        return name.equals( CasualService.class.getName() );
    }
}
```

The order of the calls in *ServiceHandler* is as follows:
* before - perform actions prior to service invocation.
* convertRequestParams - convert the request parameters prior to service invocation.
* actual service call
* handleSuccess - if service call was successful
* handleError - only if service call triggers some exception
* after - perform actions after the service invocation. This is always called, regardless of outcome of service call.

Extension method implementations should never allow exceptions be thrown.

Note that before has to return something derived from ServiceHandlerExtensionContext, this is where you would 
store any eventual - per call, state.
If you do not have a need to do that you can just leave the default implementation which returns a static instance
of DefaultServiceHandlerExtensionContext.

### Startup configuration

Depending on the configuration mode for inbound startup mode, you can control when the inbound server starts accepting requests.
This may be useful in situations where your inbound applications are slow to deploy and you want to ensure that 
when casual servers perform a first domain discovery that all/certain inbound services are already registered.

There are three modes for inbound startup:
* `immediate` - start immediately without waiting for any inbound services to be registered. (Default).
* `trigger` - start when the trigger app is deployed - see mode details below.
* `discover` - start after all the specified inbound startup services are registered and available. 

#### Immediate Startup mode

The `immediate` startup mode is the default behaviour. 
It starts the casual inbound server as soon as possible during the deployment of the casual JCA application. 
It does not wait for any inbound services to be registered.
As a result it is possible for inbound domain discovery requests to be recieved whilst other applications are
still being deployed and therefore before their contained inbound services are registered.
In this scenario a subsequent domain discovery request will be required to `discover` those services.

#### Trigger Startup mode

The `trigger` mode is the same as the `discover` mode, however it waits for a "single special service" 
to be registered as a trigger to starting the inbound server.
This special service is packaged within the `casual-inbound-startup-trigger-app.ear`, which can be deployed "last"
on the application server. When this application is deployed it will register the "special" service and trigger
the starting of the inbound server.
This mode is intended for where you don't want to explicitly state which services you wish to wait for at startup, 
which is possible using `discover` mode.

#### Discover Startup mode

The `discover` startup mode ensures that all inbound startup services that are specified in the configuration are 
registered and therefore up and running before allowing the inbound server to start accepting requests.
The inbound startup services are provided as a list of service names. See [Configuration](README.md#configuration) for more details. 
If there are no services defined in the configuration, then `discover` mode is equivalent to `immediate` mode.

#### Initial inbound startup delay
If initial inbound startup delay is configured ( see [Configuration](README.md#configuration) ) - 
then that always it will always delay the startup by that amount of seconds regardless of startup mode.
