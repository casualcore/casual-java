# Configuration

Casual supports the following configuration options which can be set using environment variables.

* `CASUAL_API_FIELDED_ENCODING` - Set encoding for fielded buffers. Default `UTF8`.
* `CASUAL_FIELD_TABLE` - Set path for fielded buffer definition. Mandatory.
* `CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER` - Enable logging for outbound network. Default `false`.
* `CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER` - Enable logging for inbound network. Default `false`.
* `CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER` - Enable logging for reverse inbound network. Default `false`.
* `CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL` - The logging level for the outbound logging handler. Default `INFO` - see below for more information regarding available levels.
* `CASUAL_INBOUND_NETTY_LOGGING_LEVEL` - The logging level for the inbound logging handler. Default `INFO` - see below for more information regarding available levels.
* `CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL` - The logging level for the reverse inbound logging handler. Default `INFO` - see below for more information regarding available levels.
* `CASUAL_DOMAIN_NAME` - Set name for the domain/app server. Default empty (`""`).
* `CASUAL_CONFIG_FILE` - Set path for casual configuration file. If not provided, default are used.
* `CASUAL_INBOUND_STARTUP_MODE` - Set mode for inbound startup. Default `immediate`. Alternatives `trigger`, `discover`.
    See [Inbound Startup Configuration](inbound.md#startup-configuration) for more details.
* `CASUAL_OUTBOUND_USE_EPOLL` - If set to true, Netty uses epoll instead of NIO - deprecated, please use `CASUAL_USE_EPOLL` instead.
* `CASUAL_INBOUND_USE_EPOLL` - If set to true, Netty uses epoll instead of NIO - deprecated, please use `CASUAL_USE_EPOLL` instead.
* `CASUAL_USE_EPOLL` - If set to true, Netty uses epoll instead of NIO, this goes for outbound, inbound and reverse inbound.
* `CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS` - Delay the startup of the inbound server. Default is no delay.
* `CASUAL_UNMANAGED_SCHEDULED_EXECUTOR_SERVICE_POOL_SIZE` - The pool size of casual's scheduled executor service instance. Defaults to 10.
* `CASUAL_UNMANAGED` - If set to false, outbound and reverse inbound will use a managed executor service.

NB - if a `CASUAL_CONFIG_FILE` is provided, it take precedence over the `CASUAL_INBOUND_STARTUP_MODE` setting.
However, if some configuration is missing from the configuration file but has a setting via an environment variable then this will be used before any hardcoded default setting.

If `CASUAL_INBOUND_STARTUP_INITIAL_DELAY_SECONDS` is configured then it will always delay the startup by that amount of seconds regardless of startup mode.
That is - discovery is not running concurrently and the delay is always executed after any potential discovery.

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

Within the casual configuration file you can currently specify:
* Inbound startup mode 
* Inbound startup services
* Domain name

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
