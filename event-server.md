# Event server

The purpose of the event server is to be able to access information regarding service calls as they happen - both inbound and outbound.

Example of server output:
```json
{"service":"javaEcho","parent":"","pid":154,"execution":"3fc1b94bbf6344408491902b0c247e33","transactionId":"ffff0af400d32c59fb9165e9ca860000668e77696c64666c79:ffff0af400d32c59fb9165e9ca86000066920000000100000000:131077","start":1710245620960234,"end":1710245620975234,"pending":0,"code":"OK","order":"S"}
```

When using gateway protocol version >= 1.3, the output includes `spanId` and `parentSpanId`:
```json
{"service":"javaEcho","parent":"callingService","pid":154,"execution":"3fc1b94bbf6344408491902b0c247e33","transactionId":"ffff0af400d32c59fb9165e9ca860000668e77696c64666c79:ffff0af400d32c59fb9165e9ca86000066920000000100000000:131077","start":1710245620960234,"end":1710245620975234,"pending":0,"order":"S","spanId":"00a1b2c3d4e5f678","parentSpanId":"0011223344556677","code":"OK"}
```

The format follows casuals [example](http://casual.laz.se/documentation/en/1.6/middleware/event/documentation/service.log.html?highlight=event) but instead of the output being a plain string it is in JSON-format.

## How to enable it

By default the event server does not run unless you configure it to do so.
This you do in your casual-config.json file as follows:
```json
{
  "eventServer":{
      "portNumber": 7698,
      "useEpoll": true
  }
}
```

## How to connect to the server

You need to send a logon package that looks as follows:
```json
{"message":"HELLO"}
```

