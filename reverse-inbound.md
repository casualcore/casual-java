# Reverse Inbound

## What is it?

*Reverse Inbound* is not specified in the *JCA-specification* at all, it is an addition in *casual-jca* as the opposite
( *Reverse Outbound* ) is available in casual.

## How does it work

By specifying a reverse inbound section in the casual configuration file, such as:
```json
{
  "reverseInbound":[
    {"address": {"host":"some-reverse-outbound", "port":7771},
      "size": 4
    }
  ]
}
```

On *casual-jca* startup, this configuration is read and for each configuration an *outbound* connection towards the defined
host:port. If no size is specified then only one such connection is created per configuration.

After the connection has succeeded, this connection now works as it was a normal *inbound* connection.

