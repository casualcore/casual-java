package se.kodarkatten.casual.jca.inbound.handler.jndi

import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse
import spock.lang.Shared
import spock.lang.Specification

import javax.naming.Context
import javax.naming.NamingException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.charset.StandardCharsets

class JndiHandlerTest extends Specification
{
    @Shared JndiHandler instance
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


    def setup()
    {
        serialisedCall = JavaServiceCallDefinition.of( methodName, methodParam )

        json = jp.toJson( serialisedCall )

        message = InboundRequest.of( jndiServiceName, JsonBuffer.of( json ).getBytes() )

        instance = new JndiHandler( )

        context = Mock( Context )
        Class[] c = new Class[1]
        c[0] = SimpleService.class
        proxyService = Mock( SimpleService )

        jndiObject = (Proxy)Proxy.newProxyInstance(
                JndiHandlerTest.getClassLoader(),
                c,
                new ForwardingInvocationHandler( proxyService )
        )

        instance.setContext( context )
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
        String json = new String( reply.getPayload().get( 0 ), StandardCharsets.UTF_8 )
        jp.fromJson( json, String.class ) == methodParam
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
        String json = new String( reply.getPayload().get( 0 ), StandardCharsets.UTF_8 )
        json.contains( exceptionMessage )
    }

    def "Call service with multiple payloads fails."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }
        List<byte[]> payload = Arrays.asList(
                JsonBuffer.of( json ).getBytes().get( 0 ),
                JsonBuffer.of( json ).getBytes().get( 0 ) )

        message = InboundRequest.of( jndiServiceName, payload )

        when:
        InboundResponse reply = instance.invokeService( message )

        then:
        0 * proxyService.echo( _ )

        !reply.isSuccessful()
        String json = new String( reply.getPayload().get( 0 ), StandardCharsets.UTF_8 )
        json.contains( "Payload size" )
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

    class ForwardingInvocationHandler implements InvocationHandler
    {
        Object target

        ForwardingInvocationHandler(Object target )
        {
            this.target = target
        }

        @Override
        Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            Class[] paramClass = null
            if( args != null )
            {
                paramClass = new Class[args.length]
                for( int i=0; i< args.length; i++ )
                {
                    paramClass[i] = args[i].getClass()
                }
            }
            return target.getClass().getMethod( method.getName(), paramClass ).invoke( target, args );
        }
    }
}
