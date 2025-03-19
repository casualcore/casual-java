# Changelog
This is the changelog for *casual java* and all changes are listed in this document.


## [3.3.1] - 2025-01-13
- Feature/3.2/unmarshall all null values (#143)
- handle null values correctly
This fixes #131

## [3.3.0] - 2024-12-10
- Feature/3.2/code needed for conversation support in casual caller (#141)
- Instead of returning a Conversation we now return a TpConnectReturn which potentially wraps a ErrorState and a potential Conversation if ErrorState == ErrorState.OK.
It is auto closable and should be used in a try with resources context.

The user should check the ErrorState before using the Conversation, this is in the same way as tp(a)call and ServiceReturn.

This code will be used in casual caller to support conversation as well.

## [3.2.50] - 2024-12-10
- Feature/3.2/initialise fieled on startup (#140)
- Try to initialise fieled on startup if the user has provided a configuration for the file.

## [3.2.49] - 2024-12-09
- Feature/3.2/bundle gson (#138)
- Make sure that gson is not needed in the global module since this polluted all deployed applications with that specific gson implementation.

Two new modules:

casual-json-provider - provides our JsonProvider implementation as well as the dependency on our gson version
casual-api-spi-impl - Module that wraps all our spi modules, currently only casual-json-provider. This is the module that other applications should depend on.

## [3.2.48] - 2024-11-28
- Bugfix/3.2/inbound discovery with custom service handlers (#130)
- Utilise all available ServiceHandlers to check if services are available during startup of inbound in Discover mode.

## [3.2.47] - 2024-11-21
- Bugfix/3.2/configuration mode not honoured (#126)
- Refactoring configuration functionality to ensure correctness of resulting configuration values.
Simplifying configuration to flatter property based structure for ease of retrieval.
Consolidating location for definition of defaults and reading of environment varilables for all configuration options.

## [3.2.46] - 2024-11-01
- trid unique per resource, not global (#127)
- For wildfly, the trid should be unique per resource - not global as it is for wls as wls handles it differently.

## [3.2.45] - 2024-10-07
- Feature/3.2/java 17 sonar fixes (#123)
- Java 17 sonar fixes.
There are still 2 issues found, that ServiceReturn and CasualFielded should be made into records.
That is correct, however they live in the API and we would need to do a major release to fix that.

Thus they should remain as issues, no supression, until we make a major release can convert them into records.

## [3.2.44] - 2024-09-27
- Feature/3.2/execution sticky (#122)
- In the service call API, expose methods, tp(a)call, that additionally accepts a execution id.

casual-caller will make use of this new API when n number of service calls are made in the same transaction and sticky is enabled.
The same execution will then be used for all service calls in the same transaction and it will help with traceability.

## [3.2.43] - 2024-09-09
- bugfix/3.2/reverse inbound make sure inet address is not resolved and cached
- Let netty handle DNS-resolution

This fixes a bug where we would cache a resolved InetSocketAddress.

The intention is now clear, we never hand over a resolved InetSocketAddress to netty.

## [3.2.42] - 2024-06-12
- Feature/3.2/reverse inbound share event loop group (#118) (#119)
- All reverse inbound clients now shares the same event loop group instead of creating a new per connection
This works the same way as it currently does for outbound connections.
New root property useEpoll - can also be set via env var CASUAL_USE_EPOLL
If set to true, epoll is used for outbound, inbound and reverse inbound
New root property unmanaged - can also be configured via env var CASUAL_UNMANAGED
If set to true, outbound and reverse inbound runs using a managed executor ( only supported on jboss/wildfly currently)
Netty version bump to 4.1.110.Final

The deprecated configuration options re epoll and unmanaged will be removed in a future version.

## [3.2.41] - 2024-05-15
- feature/3.2/event client do not set json object limit (#116)
- no user limit
it is actually max buffer length, default is fine

## [3.2.40] - 2024-05-15
- bugfix/event client receives close in shutdown inside jvm (#115)
- EventClient passes along itself in the disconnected callback
EventClient guard so that it does notify observer if the user called close
EventServer close - changes so that it works in test as well

---------

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [3.2.39] - 2024-05-08
- Feature/3.2/casual test (#114)
- Initial embedded server structure.

Added event client tests connect and receiving messages. Random port allocation for event server. Enable concurrent instances using domainId. Fix issue with event client using linux specific netty channel and group.

Add multi client test. Bump version.

PR comment fixes. Fix newBuilder. Revert * imports. Code formatting. Explicit null check on publishEvent.

## [3.2.38] - 2024-05-02
- allow real, optional, method as well (#112)
- allow real, optional, method as well

## [3.2.37] - 2024-05-02
- Bugfix/3.2/log tool handle all messages (#111)
- handle all message types and test that we do

## [3.2.36] - 2024-04-23
- Feature/3.2/event client (#108)
- Event client, simple and efficient client to handle service call events.
Now also with a connect future for usage in test clients.

## [3.2.35] - 2024-04-16
- reverse-inbound: connection backoff
- Added a backoff delay before retrying connections when reverse-inbound can't connect.

This fixes an issue where connection retries were performed rapidly which could cause exessive logging and even cause out-of-memory errors on servers with small heap.

The backoff will increment the delay time for each failure for a specific connection, up to some configurable max connection backoff for reverse-inbound (30 seconds).

## [3.2.34] - 2024-04-15
- Feature/3.2/event service testability (#105)
- Update to allow withStart and withEnd on event. Refactor time functions into InstantUtil.

Enable configuration for event server shutdown duration.

Make EventServer constructor private.

## [3.2.33] - 2024-03-28
- Feature/3.2/sphinx docs (#103)
- sphinx documentation

## [3.2.32] - 2024-03-19
- feature/3.2/metric events (#99)
- Event Server - will only ever be available on 3.2
Sonar version bump
Gradle version bump
System lambda version bump

---------

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [3.2.31] - 2024-03-01
- do not run prepare, commit, rollback on netty threads (#97)
- Reverse inbound is in fact a client and as such we do not want prepare/commit/rollback to run on any of nettys networking threads.
This is especially true since in fact an inbound prepare can result in an oubound prepare call via CasualXAResource, that then blocks that thread until an answer is received.

All of the operations are expected to be short running as they are not service calls, service calls stil run on the supplied work manager.

## [3.2.30] - 2024-02-06
- bugfix/3.2/reverse-inbound-use-own-worker-group

## [3.2.29] - 2024-01-30
- feature/3.2/logging
- Log all available information in inbound message listener
Enable setting the netty log handler level for outbound/inbound and reverse inbound

## [3.2.27] - 2023-11-16
- Buggfix/3.2/honour tpnoreply
- A non transactional tpacall with flags TPNOREPLY should never return nor expect an answer

## [3.2.26] - 2023-10-19
- buggfix/3.2/pass real method to buffer handlers (#81)
- The real method needs to be available for buffer handlers since annotations may only exist there and not on the proxy method.
We also future proof the API by using a wrapper class for the inbound request info that can be further expanded upon when need be.

Other changes:
for spock using Java 17
using bytebuddy instead of cglib since it gives better error messages when things go wrong
up version of netty to 4.1.100

## [3.2.25] - 2023-08-31
- Feature/3.2/topology update
- Bump and support protocol version 1.2
It adds a topology updated event that we expose via CasualConnection.

It means that the services known for that connection has changed in some way.
This is useful information for any application that caches such data.

## [3.2.24] - 2023-06-12
- feature/3.2/casual protocol 1.1 outbound (#76) (#77)
- Handle domain disconnect request by casual.
Note, this is for outbound connections only.

## [3.2.23] - 2023-05-17
- Feature/3.2/inbound log waiting for service registration
- If the inbound startup mode is discovery, log the initial set of services and keep logging which service registrations that inbound is still waiting for.
This is due to a demand from a customer and it is not an unreasonable request.

## [3.2.22] - 2023-04-06
- name change of SPI Extension file (#67)

## [3.2.21] - 2023-04-05
- netty bump to 4.1.91.Final

## [3.2.20] - 2023-04-05
- missed comments

## [3.2.19] - 2023-04-05
- Jakarta EE note

## [2.2.34] - 2024-10-09
- Feature/2.2/execution sticky (#124)
- In the service call API, expose methods, tp(a)call, that additionally accepts a execution id.

casual-caller will make use of this new API when n number of service calls are made in the same transaction and sticky is enabled.
The same execution will then be used for all service calls in the same transaction and it will help with traceability.

## [2.2.33] - 2024-09-10
- Let netty handle DNS-resolution (#121)
- This fixes a bug where we would cache a resolved InetSocketAddress.

The intention is now clear, we never hand over a resolved InetSocketAddress to netty.

## [2.2.32] - 2024-06-12
- Feature/2.2/reverse inbound share event loop group (#118)
- All reverse inbound clients now shares the same event loop group instead of creating a new per connection
This works the same way as it currently does for outbound connections.
New root property useEpoll - can also be set via env var CASUAL_USE_EPOLL
If set to true, epoll is used for outbound, inbound and reverse inbound
New root property unmanaged - can also be configured via env var CASUAL_UNMANAGED
If set to true, outbound and reverse inbound runs using a managed executor ( only supported on jboss/wildfly currently)
Netty version bump to 4.1.110.Final

The deprecated configuration options re epoll and unmanaged will be removed in a future version.

## [2.2.31] - 2024-05-06
- allow real optional method as well (#113)
- allow real optional method as well

## [2.2.30] - 2024-04-19
- Bugfix/2.2/log tool handle all messages (#109)
- handle all message types and test that we do

## [2.2.29] - 2024-04-16
- reverse-inbound: connection backoff
- Added a backoff delay before retrying connections when reverse-inbound can't connect.

This fixes an issue where connection retries were performed rapidly which could cause exessive logging and even cause out-of-memory errors on servers with small heap.

The backoff will increment the delay time for each failure for a specific connection, up to some configurable max connection backoff for reverse-inbound (30 seconds).

## [2.2.28] - 2024-03-04
- do not run prepare, commit, rollback on netty threads (#97) (#98)
- Reverse inbound is in fact a client and as such we do not want prepare/commit/rollback to run on any of nettys networking threads.
This is especially true since in fact an inbound prepare can result in an oubound prepare call via CasualXAResource, that then blocks that thread until an answer is received.

All of the operations are expected to be short running as they are not service calls, service calls stil run on the supplied work manager.

## [2.2.27] - 2024-02-06
- bugfix/2.2/reverse-inbound-use-own-worker-group

## [2.2.26] - 2024-02-06
- feature/3.2/logging (#90)
- Log all available information in inbound message listener
Enable setting the netty log handler level for outbound/inbound and reverse inbound

## [2.2.25] - 2023-11-16
- Buggfix/2.2/honour tpnoreply (#83)
- A non transactional tpacall with flags TPNOREPLY should never return nor expect an answer

## [2.2.24] - 2023-10-19
- buggfix/2.2/pass real method to buffer handlers (#82)
- The real method needs to be available for buffer handlers since annotations may only exist there and not on the proxy method.
We also future proof the API by using a wrapper class for the inbound request info that can be further expanded upon when need be.

Other changes:
spock, same changes as for 3.2
using bytebuddy instead of cglib since it gives better error messages when things go wrong
up version of netty to 4.1.100

version bump

## [2.2.23] - 2023-08-31
- Feature/2.2/topology update
- Bump and support protocol version 1.2
It adds a topology updated event that we expose via CasualConnection.

It means that the services known for that connection has changed in some way.
This is useful information for any application that caches such data.

## [2.2.22] - 2023-06-12
- Feature/2.2/casual protocol 1.1 outbound (#76)
- Handle domain disconnect request by casual.
Note, this is for outbound connections only.

## [2.2.21] - 2023-05-17
- Feature/2.2/inbound log waiting for service registration
- If the inbound startup mode is discovery, log the initial set of services and keep logging which service registrations that inbound is still waiting for.
This is due to a demand from a customer and it is not an unreasonable request.

## [2.2.20] - 2023-04-05
- netty bump to 4.1.91.Final

## [2.2.19] - 2023-04-04
- casual caller external (#66)
- casual caller is now external and lives here:
https://github.com/casualcore/casual-caller
test code moved here:
https://github.com/casualcore/casual-java-test-apps

## [2.2.18] - 2023-03-28
- wls needs Stateless annotation (#65)
- It works fine on wildfly without the Stateless annotations but for wls that is not so.

## [2.2.17] - 2023-03-24
- Feature/make casual service handler extensible (#63)
- A 3rd party user may want to extend the functionality of CasualServiceHandler.
This is accomplished by implementing ServiceHandlerExtension which is loaded via SPI.

This feature has been verified by a 3rd party that it fulfills their needs and we think it will most likely fulfill other 3rd parties needs as well.

Stateless is removed from any SPI classes as that was something that accidently was not removed once we moved to using SPI.

---------

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [2.2.16] - 2023-03-08
- Feature/maven central publishing#17 (#62)
- Enable publishing to maven central, requires gpg signing key and uses manual staging release through sonar type ui.

## [2.2.15] - 2023-02-07
- Feature/stop jndi search timer if trigger mode and inbound started (#61)
- If inbound mode is trigger and inbound has started, there is no need for the JNDI search timer to continue running.
This since when running in trigger mode, no dynamic deployments are expected.

## [2.2.14] - 2023-02-01
- Feature/casual xa is same rm (#60)
- This feature solves issue #59 
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
- Feature/discovery request names sorted and unique (#56)
- Domain discovery request messages should always have the service/queue names unique and sorted.
This is due to a gateway protocol change.

## [2.2.12] - 2023-01-13
- Reduce logging from WARN to FINEST when jndi lookup fails - add exception stack. (#55)

## [2.2.11] - 2023-01-11
- Feature/optional inbound delay start of server (#53)
- This is an optional setting to help with a problem that we've only seen on wls.
When starting the appserver, during the call to endPointActivation on the resource adapter, if there are incoming domain discoveries it may happen that we do not get a handle via the MessageEndpointFactory.
This then throws, which is according to the specification.
However, it never self heals - that is, the activation never completes.

With this optional setting a user can set an initial so that we delay the start of the inbound server.
The endpointActivation call from the appserver runs in another thread and should thus always be able to become activated and not trip over itself.

## [2.2.10] - 2022-12-16
- log start inbound exception as SEVERE (#52)
- xceptions when starting inbound should be logged as SEVERE not WARNING

## [2.2.9] - 2022-11-28
- Feature/more logging (#50)
- prepared, log if XA_RDONLY
start/end log flag also by name
inbound start work, better logging overall

Co-authored-by: cklellie <christopherjkelly@gmail.com>

## [2.2.8] - 2022-11-16
- finest instead of info logging (#47)
- Use finest instead of info, we need to be careful not to accidently spam logs

## [2.2.7] - 2022-10-26
- Feature/inbound epoll (#43)
- Inbound with epoll option.

## [2.2.6] - 2022-10-25
- Feature/reverse inbound (#42)
- Reverse inbound, it is not a part of the JCA specification - however the opposite is available in casual, reverse outbound, so for symmetry reasons this feature is implemented.

## [2.2.5] - 2022-10-19
- Feature/jdk 8 11 17 (#39)
- Can use jdk 8, 11 or 17 to compile.
Still going with java 8 byte code generation.

## [2.2.4] - 2022-10-14
- Feature/netty epoll (#38)
- This enables us to choose if Netty should use NIO ( default) or epoll.

According to Netty's [documentation](https://netty.io/wiki/native-transports.html) it may give us less garbage and better throughput so it makes sense to provide the option to use it.

To enable epoll, you either set CASUAL_OUTBOUND_USE_EPOLL to true or in the outbound section of your configuration file you add - "useEpoll": true.

Note, this does affect our packaging since now netty-transport-native-epoll will always be available, even when using NIO.
That in itself is not a problem.

## [2.2.3] - 2022-10-12
- Feature/revert domain id (#37)
- Revert domain id changes.
One pool is supposed to go towards one EIS only, if need be - by configuring only one physical connection.

casual-caller reverted to implementation from tag: 2.0.2 + using the current QueueInfo API.

## [2.2.2] - 2022-10-10
- Feature/network pooling (#36)
- We provide the ability to configure the number of physical network connections per pool.
This is done by setting 2 additional attributes on the pool - NetworkConnectionPoolName and NetworkConnectionPoolSize.
This in addition to the old pool attributes, HostName and PortNumber.

If they are not configured then we still go with the 1-1 Managed Connection, physical network connection ratio.

## [2.1.3] - 2022-09-20
- Feature/pooling backed by randomness (#34)
- When connecting via a load balancer, each physical connection in a pool may be connected to different casual domains.
Thus they can each have their own set of queues and services and we need to take that into account.
We make it so that for each call we can provide the domain id so that the appserver can match on that and return a handle for which the physical connection has that exact domain id.

We record each domain id per host:port ( address) combination for all new ManagedConnections that the appserver creates.
This data can then be queried by casual-caller.
Casual-caller does this by polling, by default each 10s, and then updating the state of the world.

## [2.0.2] - 2022-07-29
- expose casual domain id and connection gone
- See https://github.com/casualcore/casual-java/issues/31

This is the first part to solve that issue.
The second part is to change casual-caller to make use of this.

## [2.0.1] - 2022-06-16
- Reduce log level from WARNING to FINEST (#30)

## [2.0.0] - 2022-06-08
- no optional parameter (#27)
- Remove any Optional parameters since we do not know if they have been consumed before being passed on.

## [1.2.1] - 2022-06-02
- Feature/gradle upgrade (#26)
- Update gradle to 7.4.2, remove propdeps plugin, fix configurations, remove warnings.

## [1.2.0] - 2022-05-09
- Feature/test app (#23)
- Start of test application with 2 exposed java services, javaEcho/javaForward. To be expanded upon.
Make Flag serializable in case it is used by value rather than by reference

## [1.1.0] - 2022-05-08
- Feature/casual caller needs to handle connection reestablished better (#21)
- When a previously known connection goes away and comes back again, rediscovery of all known services/queues ( for all connections) needs to be issued and the caches repopulated with the result of that discovery.

## [1.0.81-beta] - 2022-04-04
- Feature/more network logging (#7)
- yet more network logging

version

## [1.0.80-beta] - 2022-04-04
- always just close channel (#6)

## [1.0.78-beta] - 2022-03-30
- outbound network, only close channel if open
- outbound network only close channel if open
do not block on channel close
netty version bump to the latest 4.1.75-Final
gson version bump to the latest 2.9.0

## [1.0.77-beta] - 2022-03-23
- Feature/fielded optional allow null fill with default values (#2)
- FieldedTypeBuffer:

Allow null values that are written to be written using a set of predefined default values.

Note that this breaks the symmetry with the c++ side regarding how it works when writing fielded.

We allow this since it seems to be common in old code that people want to be able to write null values ( even though you really can not since we can not transport it).

## [1.0.76-beta] - 2022-03-18
- feature/conversation (pull request #142)
- Support conversation


Approved-by: Tobias Leo

## [1.0.75-beta] - 2022-03-17
- feature/messages-v1.1-v1.2 (pull request #141)
- Message protocol version(s)

New messages only available in protocol version 1.1, 1.2

Approved-by: Tobias Leo

## [1.0.74-beta] - 2022-03-15
- feature/inbound-startup-mode-empty-should-default-not-fail (pull request #140)
- When CASUAL_INBOUND_STARTUP_MODE is set such as:

CASUAL_INBOUND_STARTUP_MODE= 
Then reading it via System.getEnv returns an empty string as opposed to null when env var is undefined. It is expected to not fail but to return the default mode.

Approved-by: Tobias Leo

## [1.0.73-beta] - 2022-03-01
- feature/pretty-printer-branch-can-be-null-on-wls (pull request #139)
- prevent potential null pointer when logging on wls

Approved-by: Tobias Leo

## [1.0.72-beta] - 2022-02-15
- feature/marshalling-refactoring (pull request #136)
- refactoring of unmarshaller to make it more readable

Approved-by: Tobias Leo

## [1.0.71-beta] - 2022-02-04
- feature/config-use-env-var-if-config-is-missing (pull request #135)
- domain name, if using config file and missing from config - use env var before default

mode, if using config file and missing from config - use env var before default

log complete active configuration

Approved-by: Tobias Leo

## [1.0.70-beta] - 2022-02-04
- feature/marshalling-missing-features (pull request #134)
- Feature/marshalling missing features

lengthName optional for List/Arrays

Approved-by: Tobias Leo

## [1.0.69-beta] - 2022-02-02
- feature/unmanaged-option (pull request #133)
- Allow outbound unmanaged as an option


Approved-by: Tobias Leo

## [1.0.68-beta] - 2022-01-31
- feature/use-serviceloader-on-demand (pull request #132)
- use service loader on demand to protect against threading issues

Approved-by: Tobias Leo

## [1.0.67-beta] - 2022-01-28
- feature/marshalling-no-objenesis-dep (pull request #131)
- No objenesis dependency needed for marshalling

NOP-constructor needed for marshalling


Approved-by: Tobias Leo

## [1.0.66-beta] - 2022-01-18
- feature/marshalling-via-spi (pull request #127)
- Feature/marshalling via spi

provide fielded marshaller via SPI

Approved-by: Tobias Leo

## [1.0.65-beta] - 2021-11-19
- feature/xid-and-execution-id-casual-pretty-printing (pull request #126)
- casual log pretty printing

keep appservers stringversion of xid as well, it is useful when inspecting things during runtime in the appservers themself

Approved-by: Tobias Leo

## [1.0.64-beta] - 2021-11-12
- feature/conversation-messages-latest
- new version of conversational messages

Approved-by: Tobias Leo

## [1.0.63-beta] - 2021-11-10
- feature/network-handle-io-error
- For outbound, when an exception reaches the ExceptionHandler - always check if the connection is gone and if it is, inform the appserver.

Approved-by: Tobias Leo

## [1.0.62-beta] - 2021-11-10
- feature/no-queue-space
- no qspace
QueueMessage serializable


Approved-by: Tobias Leo

## [1.0.61-beta] - 2021-11-09
- Casual caller next generation

## [1.0.60-beta] - 2021-10-22
- feature/outbound-use-jee-concurrency-utils (pull request #120)
- Use a ManagedExecutorService when initializing the EventLoopGroup for netty

Only use 1 EventLoopGroup for all outbound connections

Optional configuration for Outbound with default values

Approved-by: Tobias Leo

## [1.0.59-beta] - 2021-10-05
- feature/service-handler-logging (pull request #119)
- log which service handler is being used first time for a service

Approved-by: Tobias Leo

## [1.0.58-beta] - 2021-09-07
- version bump

## [1.0.57-beta] - 2021-08-04
- feature/jndi-util-log-do-not-throw (pull request #117)
- log instead of throwing

log instead of throwing


Approved-by: Tobias Leo

## [1.0.56-beta] - 2021-06-23
- feature/jndi-util-missing-unit-tests (pull request #116)
- Feature/jndi util missing unit tests

added unit test to class that was missing unit tests all together

Approved-by: Tobias Leo

## [1.0.55-beta] - 2021-06-16
- Merged in feature/configurable-domain-name (pull request #115)
- Domain name for casual-jca configurable through environment variable to enable easier identification on the casual side with multiple connected java domains

Domain name for casual-jca configurable through environment variable to enable easier identification on the casual side with multiple connected java domains

Casual-jca should always use its own domain-name and domain-id when sending anything and the values should be fixed per domain

Added DomainInfo to the Configuration class, enabled domain name config by json file, added tests for domain name configuration both by env and by file

Removed Optional as argument

Renamed DomainInfo to Domain

Version 1.0.55-beta

Approved-by: Mathias Creutz

## [1.0.54-beta] - 2021-05-26
- feature/transactiontype-branch (pull request #114)
- BRANCH transaction type, netty version bump to latest

BRANCH transactio type, netty version bump to latest

Approved-by: Tobias Leo

## [1.0.53-beta] - 2021-05-18
- feature/missing-test-and-more-logging (pull request #113)
- Feature/missing test and more logging

Approved-by: Chris Kelly

## [1.0.52-beta] - 2021-03-17
- Merged in feature/cstringbuffer-should-always-be-serializable (pull request #110)
- CStringBuffer should always be serializable

use String for charset as attribute

This resolves issue #64

Approved-by: Tobias Leo
Approved-by: Chris Kelly

## [1.0.51-beta] - 2021-03-01
- Merged in feature/version-bumps (pull request #108)
- versionbump for netty and gson

versionbump for netty and gson

Approved-by: Chris Kelly

## [1.0.50-beta] - 2021-03-01
- Merged in feature/optional-delay-startup-inbound (pull request #107)
- Feature/optional delay startup inbound

Refactor configuration to api. Add trigger startup app.

Replace system rules with system lambda. Refactoring.

Merge branch 'dev' into feature/optional-delay-startup-inbound

Documentation

Sonar qube fixes.

Update Documentation.

Merge branch 'dev' into feature/optional-delay-startup-inbound

Custom exceptions, documentation, no mapped name, no app java plugin, version, null check deactivation.

Update copyright to new format. Only current year new files, never change after that.

Final two reverts on copyright updates.

Approved-by: Tobias Leo
Approved-by: Mathias Creutz

## [1.0.49-beta] - 2021-02-23
- Merged in feature/keep-outbound-definitions-in-ra-xml (pull request #105)
- keep outbound class definitions in ra.xml

keep outbound class definitions in ra.xml

Approved-by: Tobias Leo
Approved-by: Chris Kelly

## [1.0.48-beta] - 2021-02-18
- Merged in feature/wls-xml-no-outbound-config (pull request #104)
- Feature/wls xml no outbound config

no outbound config in wls xml

version bump

Approved-by: Chris Kelly

## [1.0.47-beta] - 2021-02-18
- Merged in feature/inbound-server-keep-alive-on-child-handler (pull request #103)
- keep alive on child handler for inbound

keep alive on child handler for inbound

This resolves issue #62

Approved-by: Tobias Leo
Approved-by: Chris Kelly

## [1.0.46-beta] - 2021-02-15
- Merged in feature/inbound-keep-alive (pull request #102)
- SO_KEEPALIVE on inbound

SO_KEEPALIVE on inbound

This resolves issue #59

Approved-by: Chris Kelly

## [1.0.45-beta] - 2021-02-12
- feature/xa-resource-recover-return-empty-list (pull request #101)
- return empty xid array for recover

return empty xid array for recover

version bump

This resolves issue #61

Approved-by: Chris Kelly

## [1.0.44-beta] - 2021-02-12
- feature/cleanup-xa-resource (pull request #100)
- Feature/cleanup xa resource

xid association

version bump

This resolves issue #60
Approved-by: Chris Kelly

## [1.0.43-beta] - 2021-02-11
- feature/honour-timeout-set (pull request #99)
- This resolves issue #57

pass along timeout
Use nanos, protocol is wrong - Fredrik will update it

Approved-by: Chris Kelly
Approved-by: Tobias Leo

## [1.0.42-beta] - 2021-01-25
- Feature/handle casual disconnect
- For outbound, casual-jca is the client with respect to a casual domain that is the server.When the server goes away, for whatever reason, those managed connections that had a connection to that server needs to be destroyed and recreated by the application server.

We did not behave as good citizens in this respect but with this feature we do handle it in a good way.

This resolves Issue #55

Approved-by: Tobias Leo
Approved-by: Chris Kelly

## [1.0.41-beta] - 2020-11-18
- Merged in feature/casual-inbound-handler-api-addition (pull request #97)
- api addition

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.40-beta] - 2020-11-09
- Merged in feature/proper-env-var (pull request #96)
- correct naming for fielded encoding env var

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.39-beta] - 2020-11-03
- Merged in feature/multiple-ejbs-same-signiture (pull request #95)
- Feature/multiple ejbs same signiture

Use found jndi name to only search that proxy.

Approved-by: Mathias Creutz

## [1.0.38-beta] - 2020-11-02
- Merged in feature/package-netty-in-rar (pull request #94)
- JCA application packaging that works for both weblogic and wildfly

package netty in rar

also works on wls

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.37-beta] - 2020-10-28
- Merged in feature/fixes-after-sonatype-gradle-scan (pull request #92)
- bumping netty version due to security issues with older version

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.36-beta] - 2020-10-20
- Merged in feature/fielded-char (pull request #91)
- fielded char

byte is signed, char is unsigned

use Byte.toUnsignedInt method for clarity

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.35-beta] - 2020-10-16
- Merged in feature/ra-xml (pull request #90)
- ra.xml, needed for wls and no side effect for wildfly

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.34-beta] - 2020-09-23
- Merged in feature/service-buffer-only-one-disallowed-case (pull request #89)
- empty type but with payload is the only not ok case

empty type but with payload is the only not ok case

version bump

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.33-beta] - 2020-09-22
- Merged in feature/netty-flags-as-env-not-java-properties (pull request #88)
- Feature/netty flags as env not java properties

use env instead of properties

nope

version bump, naming changes to fit as env variable names

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.32-beta] - 2020-03-24
- Merged in feature/missing-transaction-enum-value (pull request #87)
- missing enum value

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.31-beta] - 2020-03-12
- Merged in buggfix/casual-c-string (pull request #86)
- Buggfix/casual c string

cstring buffer fix, downgrading spock so that tests runs again

1.0.31-beta

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.30-beta] - 2020-03-03
- Merged in feature/cstringbuffer-charset-be-explicit (pull request #85)
- Feature/cstringbuffer charset be explicit

Make charset usage explicit, also when using default charset

v1.0.30-beta

some more null tests

show intent of non default charset in test

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.29-beta] - 2020-02-05
- Merged in feature/cstring-nulltermination-validation (pull request #84)
- Change null terminator validation of CStringBuffer. Issue #52

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.28-beta] - 2020-02-04
- Merged in feature/use-https-for-spring--bump-spock-version (pull request #83)
- Feature/use https for spring  bump spock version

https for spring repo, bump spock to latest version

1.0.28-beta

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.27-beta] - 2019-09-16
- feature/casual-connection-wrap-exceptions (pull request #82)
- Feature/casual connection wrap exceptions

wrap up exceptions
version

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.26-beta] - 2019-09-02
- feature/bufferhander-expose-servicecallinfo (pull request #81)
- expose ServiceCallInfo

expose ServiceCallInfo

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.25-beta] - 2019-07-26
- Merged in feature/jdk-11 (pull request #80)
- Feature/jdk 11, use nanoseconds

JDK 11 fixes
date util for nanoseconds

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.24-beta] - 2019-05-19
- feature/documentation (pull request #79)
- Feature/documentation

Started

more documentation

more docs

Version bump remove import added by mistake.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.23-beta] - 2019-05-19
- Merged in feature/cleanup (pull request #78)
- Sonar Cleanup

sonarqube, 0 bugs

unused

more cleaning

Use map not ispresent with optional, remove squid

final on method params, remove unused params.

Remove connection request info from Managed Connection as never used.

remove unused future param.

Suppress serialization warning for MCF.

Complete TODOs. Fix return to empty array instead of null.

Remove serialisation. Suppress warning for null return.

Complete TODO.

Remove unused methods.

JSCD doesn't need to be serializable as it is json serialized only with Gson.

Nothing wrong with exceptions bubble back up the call stack from private methods.

Stopped modifying param variables.

Suppress as sonar is not able to see builder.

Refactored decoding for readability.

Squid review - removed unnecessary and fixed others to remove need for squid.

remove mbean

suppress serialization warning on fieldeddataimpl

Version bump and integration test fix.

Remove integration test due to external dependencies reference.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.22-beta] - 2019-05-19
- feature/handle-network-gone (pull request #77)
- Feature/handle network gone

version 1.0.22-beta

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.21-beta] - 2019-05-13
- feature/network-no-invalidation (pull request #76)
- no invalidation, use join not get

casual empty buffer NULL str, complete all exceptionally if needed

version

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.20-beta] - 2019-05-11
- feature/service-buffer-can-be-empty (pull request #75)
- This is a concept that was missed in the initial implementation

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.19-beta] - 2019-05-07
- Merged in feature/allow-empty-payload-in-service-call-reply (pull request #74)
- allow empty payload in service call reply

allow empty payload in service call reply

updated from PR.

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.18-beta] - 2019-03-08
- feature/casual-api-javadoc (pull request #73)
- casual-api javadoc

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.17-beta] - 2019-03-06
- deployment documentation
- General deployment documentation
Wildfly specific deployment documentation
Netty moved into the application

Approved-by: Chris Kelly <christopherjkelly@gmail.com>
Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.16-beta] - 2019-02-16
- Merged in bugfix/resource_adapter_selection_inbound_mdb (pull request #71)
- Force selection of casual-jca rar from casual inbound MDB

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.15-beta] - 2019-02-12
- Merged in bugfix/field_data_serialization (pull request #70)
- Implement serializable on fieldeddata interface.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.14-beta] - 2019-02-11
- Merged in bugfix/inbound_marshalling (pull request #69)
- Moved network classes out of the network protocol and into api.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.13-beta] - 2019-01-18
- Merged in feature/deployment_issue_wildfly13 (pull request #68)
- Restructure applications and naming of the application internal. Fix rar name in annotation. #45

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.12-beta] - 2019-01-15
- Merged in feature/simplify-deployment (pull request #67)
- Refactored Connection interfaces.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.11-beta] - 2019-01-14
- Merged in feature/dequeue-expose-blocking (pull request #66)
- Feature/dequeue expose blocking

expose blocking option for dequeueing

version 1.0.11-beta

QueueOptions serializable and with license

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.10-beta] - 2018-12-20
- feature/inbound-passthrough-response (pull request #65)
- Allow InboundRequest and InboundResponse with inbound service dispatch as passthrough.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.9-beta] - 2018-12-19
- Merged in feature/fielded-buffer-ok-empty (pull request #64)
- ok with empty FieldedTypeBuffer

ok with empty FieldedTypeBuffer

version

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.8-beta] - 2018-12-09
- Merged in buggfix/fielded-use-env (pull request #63)
- use env instead of java property

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.7-beta] - 2018-12-07
- Merged in feature/cstring-buffer (pull request #60)
- c-string type and convenience buffer

cstring type and buffer

missing files

arrays as list

maybe save some allocation, not sure

check if java string is already nullterminated

unittest

PR fixes, this is version 1.0.7-beta

PR fix

stupid storage

## [1.0.6-beta] - 2018-12-03
- Merged in feature/casual-buffer-default-pass-through (pull request #62)
- Allow pass through of casual buffers without unmarshalling. Issue #40.

Allow pass through of casual buffers without unmarshalling. Issue #40.

Refactored.

Method name and version change.

Approved-by: Patrick Lögdahl <plogdahl@gmail.com>
Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.5-beta] - 2018-11-30
- Merged in feature/lookup-exception-to-api (pull request #61)
- expose exception through the API

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.4-beta] - 2018-11-12
- Merged in feature/prioritised-order-for-handlers (pull request #59)
- Add priorities for the handlers and sort then before returning checking which to return. #39.

Add priorities for the handlers and sort then before returning checking which to return. #39.

Version.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.3-beta] - 2018-11-12
- Merged in feature/service-handler-setting-error-codes (pull request #58)
- Changed InboundResponse to allow error, transaction and user defined params to pass back from service handlers. #38.

Changed InboundResponse to allow error, transaction and user defined params to pass back from service handlers. #38.

Enforce constraint that buffer must always be set before build.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.2-beta] - 2018-05-21
- inbound port config
- Add inboundServerPort configuration property, removed ra.xml

remove unused imports.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.1-beta] - 2018-05-14
- Merged in feature/inbound_wildfly_v11 (pull request #56)
- Feature/inbound wildfly v11

Fixed method call 10,11,12 not tested weblogic. Other issue with transactions in 11, 12.

Revert weblogic retry, as still required.

Tidy up before PR

bump version.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

## [1.0.0-beta.299] - 2018-03-03
- Merged in feature/version-1.0.0-beta (pull request #55)
- version 1.0.0-beta

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

## [1.0.0] - 2022-05-04
- casual-caller better queue handling (#12)
- API for enqueue and dequeue now return a wrapper instaead that contains
the returned information as well as an error state (normally OK,
currently TPENOENT is also possible when queues are not found in any
avialable pool)
API for dequeue changed from list to optional of QueueMessage since
casual only ever returns one or no queue message
Handles queue lookup even when some pools are unavailable, given the
queue does not reside in the unavailable pools
In cases where multiple pool handle a queue by the same name
casual-caller will stick to the first pool it encounters that handles
the queue. This should emulate how similar weird configuration is
handled by casual proper
Improvements to CasualCallerControl mbean to supply some more
information, like valid and invalid pools

## [0.43] - 2017-09-20
- Merged in feature/network-sync (pull request #13)
- fielded, non async buffer, refactoring

fielded json, only groovy tests

buffer revert

sonar fixes to casual-api

we remove these for now

wip

wip

wip

wip

tests

fielded write wip

handle fielded cstrings in

more tests

yet more tests

buffer encoding

sonar cleanup

casualbuffer

wip

wip

don't waste information

sonar cleaning

host/port config

some cleanup

jboss

non sync, wip - tests remaining

complete

use blocking SocketChannel for outbound

sync

wip

Merged in feature/werror (pull request #12)

refactored for compilation with werror enabled.

Approved-by: Mathias Creutz <mathiascreutz@gmail.com>

Approved-by: Chris Kelly <christopherjkelly@gmail.com>

