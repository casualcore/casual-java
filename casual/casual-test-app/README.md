# Casual test application

## Casual Services exposed
* javaEcho
Echoes whatever is sent
* javaForward
Forwards the request to the service name specified by the environment variable ```JAVA_FORWARD_SERVICE_NAME```

## REST API

content-type: application/casual-x-octet

POST
```/casual/{serviceName}```

