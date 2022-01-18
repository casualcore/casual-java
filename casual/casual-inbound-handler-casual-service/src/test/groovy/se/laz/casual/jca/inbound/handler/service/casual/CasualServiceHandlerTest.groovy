/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual

import se.laz.casual.api.buffer.type.fielded.FieldedTypeBuffer
import se.laz.casual.api.flags.ErrorState
import se.laz.casual.api.flags.TransactionState
import se.laz.casual.api.service.CasualService
import se.laz.casual.api.service.ServiceInfo
import se.laz.casual.jca.inbound.handler.HandlerException
import se.laz.casual.jca.inbound.handler.InboundRequest
import se.laz.casual.jca.inbound.handler.InboundResponse
import se.laz.casual.network.messages.domain.TransactionType
import spock.lang.Shared
import spock.lang.Specification

import javax.naming.Context
import javax.naming.NamingException
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class CasualServiceHandlerTest extends Specification
{

    @Shared CasualServiceHandler instance
    @Shared Context context

    @Shared Proxy jndiObject

    @Shared InboundRequest message

    @Shared String casualServiceName = "TestEcho"
    @Shared String jndiServiceName = "se.laz.casual.test.Service"
    @Shared String methodName = "echo"
    @Shared String methodParam = "method param1"
    @Shared SimpleObject methodObject = SimpleObject.of( methodParam )
    @Shared SimpleService proxyService
    @Shared CasualServiceRegistry registryInstance
    @Shared FieldedTypeBuffer buffer


    def setup()
    {
        Method method = SimpleService.getMethod( methodName, SimpleObject.class )
        CasualService service = method.getAnnotation(CasualService.class)

        CasualServiceMetaData s = CasualServiceMetaData.newBuilder()
                .service( service )
                .serviceMethod( method)
                .implementationClass( SimpleService.class )
                .build()
        CasualServiceEntry e = CasualServiceEntry.of( casualServiceName, jndiServiceName, method )
        CasualServiceRegistry.getInstance().register( s )
        CasualServiceRegistry.getInstance().register( e )

        buffer =FieldedTypeBuffer.create().write('FLD_STRING1', methodParam )
        message = InboundRequest.of( casualServiceName, buffer )

        instance = new CasualServiceHandler( )

        context = Mock( Context )
        Class[] c = new Class[1]
        c[0] = SimpleService.class
        proxyService = Mock( SimpleService )

        jndiObject = (Proxy)Proxy.newProxyInstance(
                CasualServiceHandlerTest.getClassLoader(),
                c,
                new ForwardingInvocationHandler( proxyService )
        )

        instance.setContext( context )
    }

    def "Get Service returns a service object."()
    {
        when:
        ServiceInfo actual = instance.getServiceInfo(casualServiceName)

        then:
        actual.getServiceName() == casualServiceName
        actual.getTransactionType() == TransactionType.AUTOMATIC
        actual.getCategory() == "mycategory"
    }

    def "Get Service not in the cache throws Handler Exception."()
    {
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
        1 * proxyService.echo( methodObject ) >> {
            return methodObject
        }

        reply.getErrorState() == ErrorState.OK
        reply.getTransactionState() == TransactionState.TX_ACTIVE

        FieldedTypeBuffer actual = FieldedTypeBuffer.create( reply.getBuffer().getBytes() )
        actual == buffer
    }

    def "Call Service with buffer returns null and return empty buffer."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        InboundResponse reply = instance.invokeService( message )

        then:
        1 * proxyService.echo( methodObject ) >> {
            return null
        }

        reply.getErrorState() == ErrorState.OK
        reply.getTransactionState() == TransactionState.TX_ACTIVE

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
        1 * proxyService.echo( methodObject ) >> {
            return new RuntimeException( exceptionMessage )
        }

        reply.getErrorState() == ErrorState.TPESVCERR
        reply.getTransactionState() == TransactionState.ROLLBACK_ONLY
    }

    def "CanHandleService returns true when service exists in registry without jndi lookup."()
    {
        when:
        boolean actual = instance.canHandleService( casualServiceName )

        then:
        0 * context.lookup( _ )
        actual
    }

    def "CanHandleService returns false when service does not exist."()
    {
        when:
        boolean actual = instance.canHandleService( "invalid" )

        then:
        0 * context.lookup( _ )
        ! actual
    }

    def "isServiceAvailable returns true when service exists."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            return jndiObject
        }

        when:
        boolean actual = instance.isServiceAvailable( casualServiceName )

        then:
        actual
    }

    def "isServiceAvailable returns false when service does not exist."()
    {
        given:
        0 * context.lookup( _ )

        when:
        boolean actual = instance.isServiceAvailable( "unknown" )

        then:
        ! actual
    }

    def "isServiceAvailable returns false when service no longer does not exist."()
    {
        given:
        1 * context.lookup( jndiServiceName ) >> {
            throw new NamingException( "name doesnt exist." )
        }

        when:
        boolean actual = instance.isServiceAvailable( casualServiceName )

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

    class TestCasualService implements CasualService{
        @Override
        String name()
        {
            return casualServiceName
        }


        @Override
        String category()
        {
            return null
        }

        @Override
        Class<? extends Annotation> annotationType()
        {
            return CasualService.class
        }
    }
}
