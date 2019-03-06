# Wildfly Deployment

## Wildfly version
Note that these examples are tested with wildfly `13.0.0.Final`.

## Casual Dependencies

Create a new module, with an appropriate name e.g. `se.laz.casual`, via the jboss-cli:
```python
module add --name=se.laz.casual \
                --resources=casual-inbound-handler-api.jar:casual-fielded-annotations.jar:casual-service-discovery-extension.jar:wildfly/casual/casual-api.jar:gson-2.8.1.jar:objenesis-2.6.jar \
                --dependencies=javaee.api,sun.jdk
```

To make this module available globally you can either update the standalone.xml:
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

Added the following to the standalone.xml on your wildfly server:

```xml
<subsystem xmlns="urn:jboss:domain:resource-adapters:5.0">
    <resource-adapters>
        <resource-adapter id="casual-jca">
            <archive>
            casual-jca-app-1.0.7-beta.ear#casual-jca.rar
            </archive>
            <transaction-support>XATransaction</transaction-support>
            <connection-definitions>
                <connection-definition class-name="se.laz.casual.jca.CasualManagedConnectionFactory" jndi-name="eis/casualConnectionFactory" enabled="true" pool-name="casual-pool">
                    <config-property name="PortNumber">
                    7771
                    </config-property>
                    <config-property name="HostName">
                    192.168.99.100
                    </config-property>
                    <xa-pool>
                        <min-pool-size>5</min-pool-size>
                        <initial-pool-size>5</initial-pool-size>
                        <max-pool-size>5</max-pool-size>
                    </xa-pool>
                </connection-definition>
            </connection-definitions>
        </resource-adapter>
    </resource-adapters>
</subsystem>
```

### Via jboss-cli

Run the following command via jboss-cli:

```python
batch

set baseNode=/subsystem=resource-adapters/resource-adapter=casual-jca
$baseNode:add(archive=casual-jca-app-1.0.17-beta.ear#casual-jca.rar,transaction-support=XATransaction)

set connectionDefinitionNode=$baseNode/connection-definitions=casual-pool
$connectionDefinitionNode:add(\
    class-name=se.laz.casual.jca.CasualManagedConnectionFactory,\
    jndi-name=eis/casualConnectionFactoryTjohej,\
    min-pool-size=5, initial-pool-size=5, max-pool-size=5,\
    #interleaving=true,\
    enabled=true)

$connectionDefinitionNode/config-properties=HostName:add(value=192.168.99.100)
$connectionDefinitionNode/config-properties=PortNumber:add(value=7771)

run-batch
```

## How is Casual JCA deployed?

With the configuration of the Casual RA in place you can deploy the Casual JCA ear file via either the wildfly hot deployment mechanism or jboss-cli deployment.

### Via hot deploy
Add the Casual JCA ear file to the wildfly `deployments` folder.

### Via jboss-cli

```python
batch

deploy wildfly/customization/casual-jca-app-1.0.17-beta.ear

run-batch
```