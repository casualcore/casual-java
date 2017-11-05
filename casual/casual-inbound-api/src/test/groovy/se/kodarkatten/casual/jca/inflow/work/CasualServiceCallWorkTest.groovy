package se.kodarkatten.casual.jca.inflow.work

import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition
import se.kodarkatten.casual.api.buffer.type.JsonBuffer
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.api.flags.ErrorState
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.xa.XID
import se.kodarkatten.casual.network.io.CasualNetworkReader
import se.kodarkatten.casual.network.messages.CasualNWMessage
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader
import se.kodarkatten.casual.network.messages.CasualNWMessageType
import se.kodarkatten.casual.network.messages.parseinfo.CommonSizes
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage
import se.kodarkatten.casual.network.messages.service.ServiceBuffer
import se.kodarkatten.casual.network.utils.LocalEchoSocketChannel
import spock.lang.Shared
import spock.lang.Specification

import javax.naming.Context
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

class CasualServiceCallWorkTest extends Specification
{

    @Shared CasualServiceCallWork instance
    @Shared SocketChannel channel
    @Shared Context context

    @Shared CasualNWMessageHeader header
    @Shared CasualServiceCallRequestMessage message

    @Shared java.lang.reflect.Proxy jndiObject

    @Shared JavaServiceCallDefinition serialisedCall
    @Shared String jndiServiceName = "se.kodarkatten.casual.test.Service"
    @Shared String methodName = "echo"
    @Shared String methodParam = "method param1"
    @Shared SimpleService proxyService
    @Shared JsonProvider jp = JsonProviderFactory.getJsonProvider()
    @Shared String json


    def setup()
    {
        channel = new LocalEchoSocketChannel()

        serialisedCall = JavaServiceCallDefinition.of( methodName, methodParam )

        json = jp.toJson( serialisedCall )

        message = CasualServiceCallRequestMessage.createBuilder()
                        .setXid( XID.of())
                        .setExecution(UUID.randomUUID())
                        .setServiceName( jndiServiceName )
                        .setServiceBuffer( ServiceBuffer.of( "json",
                                                JsonBuffer.of(
                                                        json )
                                                        .getBytes() ) )
                        .setXatmiFlags( Flag.of())
                        .build()

        header = CasualNWMessageHeader.createBuilder()
                    .setCorrelationId( UUID.randomUUID() )
                    .setType( CasualNWMessageType.SERVICE_CALL_REQUEST )
                    .setPayloadSize( CommonSizes.SERVICE_BUFFER_PAYLOAD_SIZE.getNetworkSize() )
                    .build()

        instance = new CasualServiceCallWork( header, message, channel )

        context = Mock( Context )
        Class[] c = new Class[1]
        c[0] = SimpleService.class
        proxyService = Mock( SimpleService )

        jndiObject = (Proxy)Proxy.newProxyInstance(
                CasualServiceCallWorkTest.getClassLoader(),
                c,
                new ForwardingInvocationHandler( proxyService )
        )

        instance.setContext( context )
    }

    def "Get header."()
    {
        expect:
        instance.getHeader() == header
    }

    def "Get message."()
    {
        expect:
        instance.getMessage() == message
    }

    def "Get socket channel."()
    {
        expect:
        instance.getSocketChannel() == channel
    }

    def "Call Service with buffer and return result."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = CasualNetworkReader.read( channel )

        then:
        1 * proxyService.echo( methodParam ) >> {
            return methodParam
        }

        reply.getMessage().getError() == ErrorState.OK
        String json = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
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
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = CasualNetworkReader.read( channel )

        then:
        1 * proxyService.echo( methodParam ) >> {
            throw new RuntimeException( exceptionMessage )
        }

        reply.getMessage().getError() == ErrorState.TPESVCERR
        String json = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
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

        message = CasualServiceCallRequestMessage.createBuilder()
                .setXid( XID.of())
                .setExecution(UUID.randomUUID())
                .setServiceName( jndiServiceName )
                .setServiceBuffer( ServiceBuffer.of( "json", payload ) )
                .setXatmiFlags( Flag.of())
                .build()
        instance = new CasualServiceCallWork( header, message, channel )
        instance.setContext( context )

        when:
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = CasualNetworkReader.read( channel )

        then:
        0 * proxyService.echo( _ )

        reply.getMessage().getError() == ErrorState.TPESVCERR
        String json = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        json.contains( "Payload size" )
    }

    def "getContext not initialised returns value"()
    {
        given:
        instance.setContext( null )

        expect:
        instance.getContext() != null
    }

    def "Release does nothing."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        instance.release()
        instance.run()
        CasualNWMessage<CasualServiceCallReplyMessage> reply = CasualNetworkReader.read( channel )

        then:
        1 * proxyService.echo( methodParam ) >> {
            return methodParam
        }

        reply.getMessage().getError() == ErrorState.OK
        String json = new String( reply.getMessage().getServiceBuffer().getPayload().get( 0 ), StandardCharsets.UTF_8 )
        jp.fromJson( json, String.class ) == methodParam
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
