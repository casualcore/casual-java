# Configuration

Casual supports the following configuration options which can be set using [environment variables](#environment-variables) and/or a [config file](#casual-config-file).

Environment variable values always take precedence over config file values, allowing for the overriding of options via the setting of environment variables at runtime.

When a configuration file is specified, using `CASUAL_CONFIG_FILE`, but the option value is not specified within the config file, the environment variable value is used.
If the environment variable is also not set, then the defaults are used, as listed in the table below.

## Environment Variables

| Name                                                    | Description                                                                           | Default                                   | Allowed Values                           |
|---------------------------------------------------------|---------------------------------------------------------------------------------------|-------------------------------------------|------------------------------------------|
| `CASUAL_CONFIG_FILE`                                    | Path for casual configuration file.                                                   |                                           |                                          |
| `CASUAL_API_FIELDED_ENCODING`                           | Encoding for fielded buffers.                                                         | `UTF-8`                                   |                                          |
| `CASUAL_FIELD_TABLE`                                    | Path for fielded buffer definition.                                                   |                                           |                                          |
| `CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER`             | Enable logging for outbound network                                                   | false                                     |                                          |
| `CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER`              | Enable logging for inbound network                                                    | false                                     |                                          |
| `CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER`      | Enable logging for reverse inbound network.                                           | false                                     |                                          |
| `CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL`                   | The logging level for the outbound logging handler.                                   | `INFO`                                    | `ERROR`,`WARN`, `INFO`, `DEBUG`, `TRACE` |
| `CASUAL_INBOUND_NETTY_LOGGING_LEVEL`                    | The logging level for the inbound logging handler.                                    | `INFO`                                    | `ERROR`,`WARN`, `INFO`, `DEBUG`, `TRACE` |
| `CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL`            | The logging level for the reverse inbound logging handler.                            | `INFO`                                    | `ERROR`,`WARN`, `INFO`, `DEBUG`, `TRACE` |
| `CASUAL_DOMAIN_NAME`                                    | Name for the domain/app server.                                                       | empty (`""`)                              |                                          |
| `CASUAL_INBOUND_STARTUP_MODE`                           | Mode for inbound startup. See [inbound](inbound.md) for more details.                 | `immediate`                               | `immediate`, `trigger`, `discover`       |
| `CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS`          | Delay the startup of the inbound server.                                              | 0                                         |                                          |
| `CASUAL_EVENT_SERVER_SHUTDOWN_QUIET_PERIOD_MILLIS`      | Quiet period during event server shutdown to wait whilst closing the channel          | 2000                                      |                                          |
| `CASUAL_EVENT_SERVER_SHUTDOWN_TIMEOUT_MILLIS`           | Timeout for the event server event loop to shutdown                                   | 15000                                     |                                          |
| `CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE` | The pool size of casual's scheduled executor service instance.                        | 10                                        |                                          |
| `CASUAL_UNMANAGED`                                      | If set to false, outbound and reverse inbound will use a managed executor service.    | false                                     |                                          |
| `CASUAL_OUTBOUND_MANAGED_EXECUTOR_SERVICE_NAME`         | Name of the managed executor to use for outbound.                                     | `java:comp/DefaultManagedExecutorService` |                                          |
| `CASUAL_OUTBOUND_MANAGED_EXECUTOR_NUMBER_OF_THREADS`    | Number of threads for the outbound executor to use.                                   | 0                                         |                                          |
| `CASUAL_USE_EPOLL`                                      | Netty uses epoll instead of NIO, this goes for outbound, inbound and reverse inbound. | false                                     |                                          |
| `CASUAL_OUTBOUND_USE_EPOLL`                             | Netty uses epoll instead of NIO - deprecated, please use `CASUAL_USE_EPOLL` instead.  | false                                     |                                          |
| `CASUAL_INBOUND_USE_EPOLL`                              | Netty uses epoll instead of NIO - deprecated, please use `CASUAL_USE_EPOLL` instead.  | false                                     |                                          |

### Inbound initial delay

If `CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS` is configured then it will always delay the startup by that amount of seconds regardless of startup mode.
That is - discovery is not running concurrently and the delay is always executed after any potential discovery.

### Network logging
Netty network log handler logging level defaults to `INFO` if not set or set to the empty string.

The available log levels are:
* `ERROR`
* `WARN`
* `INFO`
* `DEBUG`
* `TRACE`

Note, the level you set needs to be equal to or less than the level set to the package `io.netty`.
For instance, with logging level for the package `io.netty` set to `DEBUG` and `CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL` also set to `DEBUG`, that will work.
Using level `INFO` will also work where as `TRACE` would not in this case.

## Casual Config File

Within the casual configuration file you can currently specify options for:
* Inbound 
* Domain name
* Outbound
* Reverse Inbound

In the following example configuration file shows `discover` startup mode enabled with 2 startup services. 
Also domain name configured as `my-casual-java-domain`, which is equivalent to setting the `CASUAL_DOMAIN_NAME` env if environment based configuration is used.
Inbound epoll is also enabled - default false. Initial delay of inbound server is configured as well, default is no delay.

```json
{
  "inbound": {
    "startup": {
      "mode": "discover",
      "services": [
        "service1",
        "service2"
      ]
    },
    "useEpoll": true,
    "initialDelay": 30
  },
  "domain": {
    "name": "my-casual-java-domain"
  }
}
```
