# Configuration

Casual supports the following configuration options which can be set using environment variables.

* `CASUAL_API_FIELDED_ENCODING` - Set encoding for fielded buffers. Default `UTF8`.
* `CASUAL_FIELD_TABLE` - Set path for fielded buffer definition. Mandatory.
* `CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER` - Enable logging for outbound network. Default `false`.
* `CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER` - Enable logging for inbound network. Default `false`.
* `CASUAL_DOMAIN_NAME` - Set name for the domain/app server. Default empty (`""`).

* `CASUAL_CONFIG_FILE` - Set path for casual configuration file. If not provided, default are used.
* `CASUAL_INBOUND_STARTUP_MODE` - Set mode for inbound startup. Default `immediate`. Alternatives `trigger`, `discover`.
    See [Inbound Startup Configuration](inbound.md#startup-configuration) for more details.
* `CASUAL_OUTBOUND_USE_EPOLL` - If set to true, Netty uses epoll instead of NIO.

NB - if a `CASUAL_CONFIG_FILE` is provided, it take precedence over the `CASUAL_INBOUND_STARTUP_MODE` setting.
However, if some configuration is missing from the configuration file but has a setting via an environment variable then this will be used before any hardcoded default setting.

## Casual Config File

Within the casual configuration file you can currently specify:
* Inbound startup mode 
* Inbound startup services
* Domain name

In the following example configuration file shows `discover` startup mode enabled with 2 startup services. Also domain name configured as `my-casual-java-domain`, which is equivalent to setting the `CASUAL_DOMAIN_NAME` env if environment based configuration is used.
```json
{
  "inbound": {
    "startup": {
      "mode": "discover",
      "services": [
        "service1",
        "service2"
      ]
    }
  },
  "domain":{
    "name": "my-casual-java-domain"
  }
}
```