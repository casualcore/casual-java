# Wildfly Deployment

## Wildfly version
Note that these examples are tested with wildfly `26.1.3.Final`.

## Casual Dependencies

Create a new module, with an appropriate name e.g. `se.laz.casual`, via the jboss-cli:
```python
module add --name=se.laz.casual \
	--resources=/opt/jboss/wildfly/casual/casual-inbound-handler-api-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-fielded-annotations-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-service-discovery-extension-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/casual-api-${CASUAL_VERSION}.jar:/opt/jboss/wildfly/casual/gson-2.10.1.jar \
                --dependencies=javaee.api,sun.jdk
```

To make this module available globally you can either update the standalone.xml or standalone-full.xml:
```xml
<global-modules>
    <module name="se.laz.casual" />
</global-modules>
```

Or you can add it via the jboss-cli:
```python
/subsystem=ee:list-add(name=global-modules, value={name=se.laz.casual})
```

## Example Casual RA Configuration

Please note that the id of the resource adapter **must** be `casual-jca`.

### via XML

Edit the standalone.xml or standalone-full.xml on your wildfly server:

```xml
<subsystem xmlns="urn:jboss:domain:resource-adapters:6.1">
  <resource-adapters>
    <resource-adapter id="casual-jca">
      <archive>
        casual-jca-app-2.2.22.ear#casual-jca.rar
      </archive>
      <transaction-support>XATransaction</transaction-support>
      <config-property name="InboundServerPort">7773</config-property>
      <connection-definitions>
        <connection-definition class-name="se.laz.casual.jca.CasualManagedConnectionFactory" jndi-name="eis/casualConnectionFactoryDefault" enabled="true" pool-name="casual-pool">
          <config-property name="HostName">localhost</config-property>
          <config-property name="PortNumber">7774</config-property>
          <config-property name="NetworkConnectionPoolName">localhost-pool</config-property>
          <config-property name="NetworkConnectionPoolSize">1</config-property>
          <xa-pool>
            <min-pool-size>100</min-pool-size>
            <initial-pool-size>100</initial-pool-size>
            <max-pool-size>100</max-pool-size>
          </xa-pool>
        </connection-definition>
      </connection-definitions>
     </resource-adapter>
   </resource-adapters>
</subsystem>
```

### Via jboss-cli


Run the following command via jboss-cli ( note that it makes use of environment variables):

```python
embed-server --server-config=standalone-full.xml --std-out=echo

set CASUAL_VERSION=${env.CASUAL_VERSION}
set CASUAL_HOST=${env.CASUAL_HOST}
set CASUAL_PORT=${env.CASUAL_PORT}
set CASUAL_INBOUND_PORT=${env.CASUAL_INBOUND_PORT}

# Global module
/subsystem=ee:list-add(name=global-modules, value={name=se.laz.casual})
# configure casual RA
set baseNode=/subsystem=resource-adapters/resource-adapter=casual-jca
$baseNode:add(archive=casual-jca-app-$CASUAL_VERSION.ear#casual-jca.rar,transaction-support=XATransaction)
$baseNode/config-properties=InboundServerPort:add(value=$CASUAL_INBOUND_PORT)
              
set connectionDefinitionNode=$baseNode/connection-definitions=casual-pool
$connectionDefinitionNode:add(\
    class-name=se.laz.casual.jca.CasualManagedConnectionFactory,\
    jndi-name=eis/casualConnectionFactoryDefault,\
    min-pool-size=100, initial-pool-size=100, max-pool-size=100,\
    enabled=true)

$connectionDefinitionNode/config-properties=HostName:add(value=$CASUAL_HOST)
$connectionDefinitionNode/config-properties=PortNumber:add(value=$CASUAL_PORT)
$connectionDefinitionNode/config-properties=NetworkConnectionPoolName:add(value=$CASUAL_HOST-pool)
$connectionDefinitionNode/config-properties=NetworkConnectionPoolSize:add(value=1)

stop-embedded-server
```

Note, if `InboundServerPort` is not set then it defaults to port# 7772.

## How is Casual JCA deployed?

With the configuration of the Casual RA in place you can deploy the Casual JCA ear file via either the wildfly hot deployment mechanism or jboss-cli deployment.

### Via hot deploy
Add the Casual JCA ear file to the wildfly `deployments` folder.

### Via jboss-cli

```python
embed-server --server-config=standalone-full.xml --std-out=echo

deploy whatever-your-path-is/casual-jca-app-${env.CASUAL_VERSION}.ear

stop-embedded-server
```
