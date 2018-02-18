package se.kodarkatten.casual.jca.inbound.handler.service.javaee

import se.kodarkatten.casual.api.buffer.CasualBufferType
import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.api.service.ServiceInfo
import se.kodarkatten.casual.jca.inbound.handler.HandlerException
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse

import se.kodarkatten.casual.network.messages.domain.TransactionType
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer

import spock.lang.Shared
import spock.lang.Specification

import javax.naming.Context
import javax.naming.NamingException
import java.lang.reflect.Proxy
import java.nio.charset.StandardCharsets

class JavaeeServiceHandlerTest extends Specification
{
    @Shared JavaeeServiceHandler instance
    @Shared Context context

    @Shared Proxy jndiObject

    @Shared InboundRequest message

    @Shared JavaServiceCallDefinition serialisedCall
    @Shared String jndiServiceName = "se.kodarkatten.casual.test.Service"
    @Shared String methodName = "echo"
    @Shared String methodParam = "method param1"
    @Shared SimpleService proxyService
    @Shared JsonProvider jp = JsonProviderFactory.getJsonProvider()
    @Shared String json
    @Shared List<byte[]> payload


    def setup()
    {
        serialisedCall = JavaServiceCallDefinition.of( methodName, methodParam )

        json = jp.toJson( serialisedCall )

        payload = new ArrayList<>();

        payload.add( json.getBytes( StandardCharsets.UTF_8 ) )

        message = InboundRequest.of( jndiServiceName, ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), payload ) )

        instance = new JavaeeServiceHandler( )

        context = Mock( Context )
        Class[] c = new Class[1]
        c[0] = SimpleService.class
        proxyService = Mock( SimpleService )

        jndiObject = (Proxy)Proxy.newProxyInstance(
                JavaeeServiceHandlerTest.getClassLoader(),
                c,
                new ForwardingInvocationHandler( proxyService )
        )

        instance.setContext( context )
    }

    def "Get Service returns a service object."()
    {
        when:
        ServiceInfo actual = instance.getServiceInfo(jndiServiceName)

        then:
        actual.getServiceName() == jndiServiceName
        actual.getTransactionType() == TransactionType.AUTOMATIC
        actual.getCategory() == ""
    }

    def "Get Service valid name throws Handler Exception."()
    {
        given:
        1 * context.lookup( "unknown" ) >> {
            throw new NamingException( "name doesnt exist." )
        }

        when:
        instance.getServiceInfo("unknown")

        then:
        thrown HandlerException
    }

    def "Call Service with buffer and return result."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        InboundResponse reply = instance.invokeService( message )

        then:
        1 * proxyService.echo( methodParam ) >> {
            return methodParam
        }

        reply.isSuccessful()
        String json = new String( reply.getBuffer().getBytes().get( 0 ), StandardCharsets.UTF_8 )
        jp.fromJson( json, String.class ) == methodParam
    }

    def "Call Service with buffer and return null with empty service buffer result."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        InboundResponse reply = instance.invokeService( message )

        then:
        1 * proxyService.echo( methodParam ) >> {
            return null
        }

        reply.isSuccessful()
        reply.getBuffer().getBytes().isEmpty()
    }

    def "Call Service with buffer service throws exception return ErrorState.TPSVCERR."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }
        String exceptionMessage = "Simulated failure."

        when:
        InboundResponse reply = instance.invokeService( message )

        then:
        1 * proxyService.echo( methodParam ) >> {
            throw new RuntimeException( exceptionMessage )
        }

        !reply.isSuccessful()
        reply.getBuffer().getBytes().isEmpty()
    }

    def "Call service with multiple payloads fails."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }
        List<byte[]> multiPayload = Arrays.asList(
                payload.get( 0 ),
                payload.get( 0 ) )

        message = InboundRequest.of( jndiServiceName, ServiceBuffer.of( CasualBufferType.JSON_JSCD.getName(), multiPayload ) )

        when:
        InboundResponse reply = instance.invokeService( message )

        then:
        0 * proxyService.echo( _ )

        !reply.isSuccessful()
        reply.getBuffer().getBytes().isEmpty()
    }

    def "CanHandleService returns true when service exists."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        boolean actual = instance.canHandleService( jndiServiceName )

        then:
        actual
    }

    def "CanHandleService returns false when service does not exist."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            throw new NamingException( "name doesnt exist." )
        }

        when:
        boolean actual = instance.canHandleService( jndiServiceName )

        then:
        ! actual
    }

    def "isServiceAvailable returns true when service exists."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        boolean actual = instance.isServiceAvailable( jndiServiceName )

        then:
        actual
    }

    def "isServiceAvailable returns false when service does not exist."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            throw new NamingException( "name doesnt exist." )
        }

        when:
        boolean actual = instance.isServiceAvailable( jndiServiceName )

        then:
        ! actual
    }

    def "getContext not initialised returns value"()
    {
        given:
        instance.setContext( null )

        expect:
        instance.getContext() != null
    }
}
