# Changelog
This is the changelog for *casual java* and all changes are listed in this document.


## [3.3.1] - 2025-01-13
### Feature/3.2/unmarshall all null values (https://github.com/casualcore/casual-java/issues/143)

* handle null values correctly
* This fixes https://github.com/casualcore/casual-java/issues/131

## [3.3.0] - 2024-12-10
### Feature/3.2/code needed for conversation support in casual caller (https://github.com/casualcore/casual-java/issues/141)

Instead of returning a Conversation we now return a TpConnectReturn which potentially wraps a ErrorState and a potential Conversation if ErrorState == ErrorState.OK.
It is auto closable and should be used in a try with resources context.

The user should check the ErrorState before using the Conversation, this is in the same way as tp(a)call and ServiceReturn.

This code will be used in casual caller to support conversation as well.

## [3.2.50] - 2024-12-10
### Feature/3.2/initialise fieled on startup (https://github.com/casualcore/casual-java/issues/140)

* Try to initialise fieled on startup if the user has provided a configuration for the file.

## [3.2.49] - 2024-12-09
### Feature/3.2/bundle gson (https://github.com/casualcore/casual-java/issues/138)

Make sure that gson is not needed in the global module since this polluted all deployed applications with that specific gson implementation.

Two new modules:

casual-json-provider - provides our JsonProvider implementation as well as the dependency on our gson version
casual-api-spi-impl - Module that wraps all our spi modules, currently only casual-json-provider. This is the module that other applications should depend on.

## [3.2.48] - 2024-11-28
### Bugfix/3.2/inbound discovery with custom service handlers (https://github.com/casualcore/casual-java/issues/130)

* Utilise all available ServiceHandlers to check if services are available during startup of inbound in Discover mode.

## [3.2.47] - 2024-11-21
### Bugfix/3.2/configuration mode not honoured (https://github.com/casualcore/casual-java/issues/126)

* Refactoring configuration functionality to ensure correctness of resulting configuration values.
* Simplifying configuration to flatter property based structure for ease of retrieval.
* Consolidating location for definition of defaults and reading of environment varilables for all configuration options.

## [3.2.46] - 2024-11-01
### trid unique per resource, not global (https://github.com/casualcore/casual-java/issues/127)

For wildfly, the trid should be unique per resource - not global as it is for wls as wls handles it differently.

## [3.2.45] - 2024-10-07
### Feature/3.2/java 17 sonar fixes (https://github.com/casualcore/casual-java/issues/123)

Java 17 sonar fixes.
There are still 2 issues found, that ServiceReturn and CasualFielded should be made into records.
That is correct, however they live in the API and we would need to do a major release to fix that.

Thus they should remain as issues, no supression, until we make a major release can convert them into records.

## [3.2.44] - 2024-09-27
### Feature/3.2/execution sticky (https://github.com/casualcore/casual-java/issues/122)

In the service call API, expose methods, tp(a)call, that additionally accepts a execution id.

casual-caller will make use of this new API when n number of service calls are made in the same transaction and sticky is enabled.
The same execution will then be used for all service calls in the same transaction and it will help with traceability.

## [3.2.43] - 2024-09-09
### bugfix/3.2/reverse inbound make sure inet address is not resolved and cached

Let netty handle DNS-resolution

This fixes a bug where we would cache a resolved InetSocketAddress.

The intention is now clear, we never hand over a resolved InetSocketAddress to netty.

## [3.2.42] - 2024-06-12
### Feature/3.2/reverse inbound share event loop group (https://github.com/casualcore/casual-java/issues/118) (https://github.com/casualcore/casual-java/issues/119)

* All reverse inbound clients now shares the same event loop group instead of creating a new per connection
   This works the same way as it currently does for outbound connections.
* New root property useEpoll - can also be set via env var CASUAL_USE_EPOLL
* If set to true, epoll is used for outbound, inbound and reverse inbound
* New root property unmanaged - can also be configured via env var CASUAL_UNMANAGED
   If set to true, outbound and reverse inbound runs using a managed executor ( only supported on jboss/wildfly currently)
* Netty version bump to 4.1.110.Final

The deprecated configuration options re epoll and unmanaged will be removed in a future version.

## [3.2.41] - 2024-05-15
### feature/3.2/event client do not set json object limit (https://github.com/casualcore/casual-java/issues/116)

* no user limit
* it is actually max buffer length, default is fine

## [3.2.40] - 2024-05-15
### bugfix/event client receives close in shutdown inside jvm (https://github.com/casualcore/casual-java/issues/115)

* EventClient passes along itself in the disconnected callback
* EventClient guard so that it does notify observer if the user called close
* EventServer close - changes so that it works in test as well

---------

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [3.2.39] - 2024-05-08
### Feature/3.2/casual test (https://github.com/casualcore/casual-java/issues/114)

* Initial embedded server structure.

* Added event client tests connect and receiving messages. Random port allocation for event server. Enable concurrent instances using domainId. Fix issue with event client using linux specific netty channel and group.

* Add multi client test. Bump version.

* PR comment fixes. Fix newBuilder. Revert * imports. Code formatting. Explicit null check on publishEvent.

## [3.2.38] - 2024-05-02
### allow real, optional, method as well (https://github.com/casualcore/casual-java/issues/112)

* allow real, optional, method as well

## [3.2.37] - 2024-05-02
### Bugfix/3.2/log tool handle all messages (https://github.com/casualcore/casual-java/issues/111)

* handle all message types and test that we do

## [3.2.36] - 2024-04-23
### Feature/3.2/event client (https://github.com/casualcore/casual-java/issues/108)

* Event client, simple and efficient client to handle service call events.
* Now also with a connect future for usage in test clients.

## [3.2.35] - 2024-04-16
### reverse-inbound: connection backoff

Added a backoff delay before retrying connections when reverse-inbound can't connect.

This fixes an issue where connection retries were performed rapidly which could cause exessive logging and even cause out-of-memory errors on servers with small heap.

The backoff will increment the delay time for each failure for a specific connection, up to some configurable max connection backoff for reverse-inbound (30 seconds).

## [3.2.34] - 2024-04-15
### Feature/3.2/event service testability (https://github.com/casualcore/casual-java/issues/105)

* Update to allow withStart and withEnd on event. Refactor time functions into InstantUtil.

* Enable configuration for event server shutdown duration.

* Make EventServer constructor private.

## [3.2.33] - 2024-03-28
### Feature/3.2/sphinx docs (https://github.com/casualcore/casual-java/issues/103)

* sphinx documentation

## [3.2.32] - 2024-03-19
### feature/3.2/metric events (https://github.com/casualcore/casual-java/issues/99)

Event Server - will only ever be available on 3.2
Sonar version bump
Gradle version bump
System lambda version bump

---------

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [3.2.31] - 2024-03-01
### do not run prepare, commit, rollback on netty threads (https://github.com/casualcore/casual-java/issues/97)

Reverse inbound is in fact a client and as such we do not want prepare/commit/rollback to run on any of nettys networking threads.
This is especially true since in fact an inbound prepare can result in an oubound prepare call via CasualXAResource, that then blocks that thread until an answer is received.

All of the operations are expected to be short running as they are not service calls, service calls stil run on the supplied work manager.

## [3.2.30] - 2024-02-06
### bugfix/3.2/reverse-inbound-use-own-worker-group


## [3.2.29] - 2024-01-30
### feature/3.2/logging

* Log all available information in inbound message listener
* Enable setting the netty log handler level for outbound/inbound and reverse inbound

## [3.2.27] - 2023-11-16
### Buggfix/3.2/honour tpnoreply

A non transactional tpacall with flags TPNOREPLY should never return nor expect an answer

## [3.2.26] - 2023-10-19
### buggfix/3.2/pass real method to buffer handlers (https://github.com/casualcore/casual-java/issues/81)

The real method needs to be available for buffer handlers since annotations may only exist there and not on the proxy method.
We also future proof the API by using a wrapper class for the inbound request info that can be further expanded upon when need be.

Other changes:
  * for spock using Java 17
   * using bytebuddy instead of cglib since it gives better error messages when things go wrong
   * up version of netty to 4.1.100


## [3.2.25] - 2023-08-31
### Feature/3.2/topology update

Bump and support protocol version 1.2
It adds a topology updated event that we expose via CasualConnection.

It means that the services known for that connection has changed in some way.
This is useful information for any application that caches such data.

## [3.2.24] - 2023-06-12
### feature/3.2/casual protocol 1.1 outbound (https://github.com/casualcore/casual-java/issues/76) (https://github.com/casualcore/casual-java/issues/77)

Handle domain disconnect request by casual.
Note, this is for outbound connections only.

## [3.2.23] - 2023-05-17
### Feature/3.2/inbound log waiting for service registration

If the inbound startup mode is discovery, log the initial set of services and keep logging which service registrations that inbound is still waiting for.
This is due to a demand from a customer and it is not an unreasonable request.

## [3.2.22] - 2023-04-06
### name change of SPI Extension file (https://github.com/casualcore/casual-java/issues/67)


## [3.2.21] - 2023-04-05
### netty bump to 4.1.91.Final


## [3.2.20] - 2023-04-05
### missed comments


## [3.2.19] - 2023-04-05
### Jakarta EE note


## [2.2.34] - 2024-10-09
### Feature/2.2/execution sticky (https://github.com/casualcore/casual-java/issues/124)

In the service call API, expose methods, tp(a)call, that additionally accepts a execution id.

casual-caller will make use of this new API when n number of service calls are made in the same transaction and sticky is enabled.
The same execution will then be used for all service calls in the same transaction and it will help with traceability.

## [2.2.33] - 2024-09-10
### Let netty handle DNS-resolution (https://github.com/casualcore/casual-java/issues/121)

This fixes a bug where we would cache a resolved InetSocketAddress.

The intention is now clear, we never hand over a resolved InetSocketAddress to netty.

## [2.2.32] - 2024-06-12
### Feature/2.2/reverse inbound share event loop group (https://github.com/casualcore/casual-java/issues/118)

* All reverse inbound clients now shares the same event loop group instead of creating a new per connection
   This works the same way as it currently does for outbound connections.
* New root property useEpoll - can also be set via env var CASUAL_USE_EPOLL
* If set to true, epoll is used for outbound, inbound and reverse inbound
* New root property unmanaged - can also be configured via env var CASUAL_UNMANAGED
   If set to true, outbound and reverse inbound runs using a managed executor ( only supported on jboss/wildfly currently)
* Netty version bump to 4.1.110.Final

The deprecated configuration options re epoll and unmanaged will be removed in a future version.

## [2.2.31] - 2024-05-06
### allow real optional method as well (https://github.com/casualcore/casual-java/issues/113)

allow real optional method as well

## [2.2.30] - 2024-04-19
### Bugfix/2.2/log tool handle all messages (https://github.com/casualcore/casual-java/issues/109)

* handle all message types and test that we do

## [2.2.29] - 2024-04-16
### reverse-inbound: connection backoff

Added a backoff delay before retrying connections when reverse-inbound can't connect.

This fixes an issue where connection retries were performed rapidly which could cause exessive logging and even cause out-of-memory errors on servers with small heap.

The backoff will increment the delay time for each failure for a specific connection, up to some configurable max connection backoff for reverse-inbound (30 seconds).

## [2.2.28] - 2024-03-04
### do not run prepare, commit, rollback on netty threads (https://github.com/casualcore/casual-java/issues/97) (https://github.com/casualcore/casual-java/issues/98)

Reverse inbound is in fact a client and as such we do not want prepare/commit/rollback to run on any of nettys networking threads.
This is especially true since in fact an inbound prepare can result in an oubound prepare call via CasualXAResource, that then blocks that thread until an answer is received.

All of the operations are expected to be short running as they are not service calls, service calls stil run on the supplied work manager.

## [2.2.27] - 2024-02-06
### bugfix/2.2/reverse-inbound-use-own-worker-group


## [2.2.26] - 2024-02-06
### feature/3.2/logging (https://github.com/casualcore/casual-java/issues/90)

* Log all available information in inbound message listener
* Enable setting the netty log handler level for outbound/inbound and reverse inbound

## [2.2.25] - 2023-11-16
### Buggfix/2.2/honour tpnoreply (https://github.com/casualcore/casual-java/issues/83)

A non transactional tpacall with flags TPNOREPLY should never return nor expect an answer

## [2.2.24] - 2023-10-19
### buggfix/2.2/pass real method to buffer handlers (https://github.com/casualcore/casual-java/issues/82)

* The real method needs to be available for buffer handlers since annotations may only exist there and not on the proxy method.
We also future proof the API by using a wrapper class for the inbound request info that can be further expanded upon when need be.

Other changes:
  * spock, same changes as for 3.2
  * using bytebuddy instead of cglib since it gives better error messages when things go wrong
  * up version of netty to 4.1.100

* version bump

## [2.2.23] - 2023-08-31
### Feature/2.2/topology update

Bump and support protocol version 1.2
It adds a topology updated event that we expose via CasualConnection.

It means that the services known for that connection has changed in some way.
This is useful information for any application that caches such data.

## [2.2.22] - 2023-06-12
### Feature/2.2/casual protocol 1.1 outbound (https://github.com/casualcore/casual-java/issues/76)

Handle domain disconnect request by casual.
Note, this is for outbound connections only.

## [2.2.21] - 2023-05-17
### Feature/2.2/inbound log waiting for service registration

If the inbound startup mode is discovery, log the initial set of services and keep logging which service registrations that inbound is still waiting for.
This is due to a demand from a customer and it is not an unreasonable request.

## [2.2.20] - 2023-04-05
### netty bump to 4.1.91.Final


## [2.2.19] - 2023-04-04
### casual caller external (https://github.com/casualcore/casual-java/issues/66)

casual caller is now external and lives here:
https://github.com/casualcore/casual-caller
test code moved here:
https://github.com/casualcore/casual-java-test-apps

## [2.2.18] - 2023-03-28
### wls needs Stateless annotation (https://github.com/casualcore/casual-java/issues/65)

It works fine on wildfly without the Stateless annotations but for wls that is not so.

## [2.2.17] - 2023-03-24
### Feature/make casual service handler extensible (https://github.com/casualcore/casual-java/issues/63)

A 3rd party user may want to extend the functionality of CasualServiceHandler.
This is accomplished by implementing ServiceHandlerExtension which is loaded via SPI.

This feature has been verified by a 3rd party that it fulfills their needs and we think it will most likely fulfill other 3rd parties needs as well.

Stateless is removed from any SPI classes as that was something that accidently was not removed once we moved to using SPI.

---------

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [2.2.16] - 2023-03-08
### Feature/maven central publishinghttps://github.com/casualcore/casual-java/issues/17 (https://github.com/casualcore/casual-java/issues/62)

Enable publishing to maven central, requires gpg signing key and uses manual staging release through sonar type ui.

## [2.2.15] - 2023-02-07
### Feature/stop jndi search timer if trigger mode and inbound started (https://github.com/casualcore/casual-java/issues/61)

If inbound mode is trigger and inbound has started, there is no need for the JNDI search timer to continue running.
This since when running in trigger mode, no dynamic deployments are expected.

## [2.2.14] - 2023-02-01
### Feature/casual xa is same rm (https://github.com/casualcore/casual-java/issues/60)

This feature solves issue https://github.com/casualcore/casual-java/issues/59 
From the JTA spec:
"If the target transaction already has another XAResource object participating
in the transaction, the transaction manager invokes the XAResource.isSameRM
method to determine if the specified XAResource represents the same resource
manager instance. This information allows the transaction manager to group the
resource managers that are performing work on behalf of the transaction.
If the XAResource object represents a resource manager instance that has
seen the global transaction before, the transaction manager groups the newly
registered resource together with the previous XAResource object and ensures
that the same resource manager only receives one set of prepare-commit calls for
completing the target global transaction."

We store the domain id of the resource manager we are connected to during the initial handshake.
We match on the domain id, if it is the same domain id then it is the same resource manager.

## [2.2.13] - 2023-01-18
### Feature/discovery request names sorted and unique (https://github.com/casualcore/casual-java/issues/56)

Domain discovery request messages should always have the service/queue names unique and sorted.
This is due to a gateway protocol change.

## [2.2.12] - 2023-01-13
### Reduce logging from WARN to FINEST when jndi lookup fails - add exception stack. (https://github.com/casualcore/casual-java/issues/55)


## [2.2.11] - 2023-01-11
### Feature/optional inbound delay start of server (https://github.com/casualcore/casual-java/issues/53)

This is an optional setting to help with a problem that we've only seen on wls.
When starting the appserver, during the call to endPointActivation on the resource adapter, if there are incoming domain discoveries it may happen that we do not get a handle via the MessageEndpointFactory.
This then throws, which is according to the specification.
However, it never self heals - that is, the activation never completes.

With this optional setting a user can set an initial so that we delay the start of the inbound server.
The endpointActivation call from the appserver runs in another thread and should thus always be able to become activated and not trip over itself.

## [2.2.10] - 2022-12-16
### log start inbound exception as SEVERE (https://github.com/casualcore/casual-java/issues/52)

xceptions when starting inbound should be logged as SEVERE not WARNING

## [2.2.9] - 2022-11-28
### Feature/more logging (https://github.com/casualcore/casual-java/issues/50)

* prepared, log if XA_RDONLY
* start/end log flag also by name
* inbound start work, better logging overall

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [2.2.8] - 2022-11-16
### finest instead of info logging (https://github.com/casualcore/casual-java/issues/47)

Use finest instead of info, we need to be careful not to accidently spam logs

## [2.2.7] - 2022-10-26
### Feature/inbound epoll (https://github.com/casualcore/casual-java/issues/43)

Inbound with epoll option.

## [2.2.6] - 2022-10-25
### Feature/reverse inbound (https://github.com/casualcore/casual-java/issues/42)

Reverse inbound, it is not a part of the JCA specification - however the opposite is available in casual, reverse outbound, so for symmetry reasons this feature is implemented.

## [2.2.5] - 2022-10-19
### Feature/jdk 8 11 17 (https://github.com/casualcore/casual-java/issues/39)

Can use jdk 8, 11 or 17 to compile.
Still going with java 8 byte code generation.

## [2.2.4] - 2022-10-14
### Feature/netty epoll (https://github.com/casualcore/casual-java/issues/38)

This enables us to choose if Netty should use NIO ( default) or epoll.

According to Netty's [documentation](https://netty.io/wiki/native-transports.html) it may give us less garbage and better throughput so it makes sense to provide the option to use it.

To enable epoll, you either set CASUAL_OUTBOUND_USE_EPOLL to true or in the outbound section of your configuration file you add - "useEpoll": true.

Note, this does affect our packaging since now netty-transport-native-epoll will always be available, even when using NIO.
That in itself is not a problem.

## [2.2.3] - 2022-10-12
### Feature/revert domain id (https://github.com/casualcore/casual-java/issues/37)

Revert domain id changes.
One pool is supposed to go towards one EIS only, if need be - by configuring only one physical connection.

casual-caller reverted to implementation from tag: 2.0.2 + using the current QueueInfo API.

## [2.2.2] - 2022-10-10
### Feature/network pooling (https://github.com/casualcore/casual-java/issues/36)

We provide the ability to configure the number of physical network connections per pool.
This is done by setting 2 additional attributes on the pool - NetworkConnectionPoolName and NetworkConnectionPoolSize.
This in addition to the old pool attributes, HostName and PortNumber.

If they are not configured then we still go with the 1-1 Managed Connection, physical network connection ratio.

## [2.1.3] - 2022-09-20
### Feature/pooling backed by randomness (https://github.com/casualcore/casual-java/issues/34)

When connecting via a load balancer, each physical connection in a pool may be connected to different casual domains.
Thus they can each have their own set of queues and services and we need to take that into account.
We make it so that for each call we can provide the domain id so that the appserver can match on that and return a handle for which the physical connection has that exact domain id.

We record each domain id per host:port ( address) combination for all new ManagedConnections that the appserver creates.
This data can then be queried by casual-caller.
Casual-caller does this by polling, by default each 10s, and then updating the state of the world.

## [2.0.2] - 2022-07-29
### expose casual domain id and connection gone

See https://github.com/casualcore/casual-java/issues/31

This is the first part to solve that issue.
The second part is to change casual-caller to make use of this.

## [2.0.1] - 2022-06-16
### Reduce log level from WARNING to FINEST (https://github.com/casualcore/casual-java/issues/30)


## [2.0.0] - 2022-06-08
### no optional parameter (https://github.com/casualcore/casual-java/issues/27)

Remove any Optional parameters since we do not know if they have been consumed before being passed on.

## [1.2.1] - 2022-06-02
### Feature/gradle upgrade (https://github.com/casualcore/casual-java/issues/26)

* Update gradle to 7.4.2, remove propdeps plugin, fix configurations, remove warnings.

## [1.2.0] - 2022-05-09
### Feature/test app (https://github.com/casualcore/casual-java/issues/23)

* Start of test application with 2 exposed java services, javaEcho/javaForward. To be expanded upon.
* Make Flag serializable in case it is used by value rather than by reference

## [1.1.0] - 2022-05-08
### Feature/casual caller needs to handle connection reestablished better (https://github.com/casualcore/casual-java/issues/21)

* When a previously known connection goes away and comes back again, rediscovery of all known services/queues ( for all connections) needs to be issued and the caches repopulated with the result of that discovery.
