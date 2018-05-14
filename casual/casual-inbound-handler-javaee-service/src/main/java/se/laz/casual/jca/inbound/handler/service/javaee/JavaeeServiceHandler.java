/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.javaee;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.internal.thread.ThreadClassLoaderTool;
import se.laz.casual.jca.inbound.handler.HandlerException;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandlerFactory;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.network.protocol.messages.service.ServiceBuffer;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Local( ServiceHandler.class )
public class JavaeeServiceHandler implements ServiceHandler
{
    private static final Logger LOG = Logger.getLogger(JavaeeServiceHandler.class.getName());

    private Context context;

    @Override
    public boolean canHandleService(String serviceName)
    {
        return isServiceAvailable( serviceName );
    }

    @Override
    public boolean isServiceAvailable(String serviceName)
    {
        try
        {
            loadService( serviceName );
            return true;
        }
        catch( NamingException e )
        {
            return false;
        }
    }

    @Override
    public InboundResponse invokeService(InboundRequest request)
    {
        LOG.finest( ()->"Request received: " + request );
        ThreadClassLoaderTool tool = new ThreadClassLoaderTool();
        boolean success = true;
        CasualBuffer payload = ServiceBuffer.of( request.getBuffer().getType(), new ArrayList<>() );
        try
        {
            Object r = loadService(request.getServiceName());
            BufferHandler bufferHandler = BufferHandlerFactory.getHandler( payload.getType() );
            tool.loadClassLoader( r );
            payload = callService( r, request.getBuffer(), bufferHandler );
        }
        catch( Throwable e )
        {
            LOG.log( Level.WARNING, e, ()-> "Error invoking fielded: " + e.getMessage() );
            success = false;
        }
        finally
        {
            tool.revertClassLoader();
        }

        return InboundResponse.of( success, payload );
    }

    @Override
    public ServiceInfo getServiceInfo(String serviceName)
    {
        if( !canHandleService( serviceName ) )
        {
            throw new HandlerException( "Service could not be found, should control with canHandle() first." );
        }

        return ServiceInfo.of( serviceName );
    }

    private Object loadService( String jndiName ) throws NamingException
    {
        Context c = getContext();
        Object r = c.lookup( jndiName );
        LOG.finest( ()->"Found " + r.getClass() + " : " + r );
        return r;
    }

    private CasualBuffer callService( Object r, CasualBuffer payload, BufferHandler bufferHandler ) throws Throwable
    {
        Proxy p = (Proxy) r;

        ServiceCallInfo serviceCallInfo = bufferHandler.fromBuffer( p, null, payload );

        Method method = serviceCallInfo.getMethod().orElseThrow( ()-> new HandlerException( "Buffer did not provided required details about the method end point." ) );
        
        Object result = method.invoke( p, serviceCallInfo.getParams() );

        LOG.finest( ()-> "Result: " + result );
        return bufferHandler.toBuffer( result );
    }

    Context getContext() throws NamingException
    {
        if( context == null )
        {
            context = new InitialContext();
        }
        return context;
    }

    void setContext( Context context )
    {
        this.context = context;
    }
}
