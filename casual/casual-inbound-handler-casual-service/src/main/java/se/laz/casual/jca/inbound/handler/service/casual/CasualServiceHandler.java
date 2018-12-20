/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.TransactionState;
import se.laz.casual.api.service.ServiceInfo;
import se.laz.casual.internal.thread.ThreadClassLoaderTool;
import se.laz.casual.jca.inbound.handler.HandlerException;
import se.laz.casual.jca.inbound.handler.InboundRequest;
import se.laz.casual.jca.inbound.handler.InboundResponse;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandler;
import se.laz.casual.jca.inbound.handler.buffer.BufferHandlerFactory;
import se.laz.casual.jca.inbound.handler.buffer.ServiceCallInfo;
import se.laz.casual.jca.inbound.handler.service.ServiceHandler;
import se.laz.casual.network.messages.domain.TransactionType;
import se.laz.casual.network.protocol.messages.service.ServiceBuffer;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttributeType;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static se.laz.casual.jca.inbound.handler.service.casual.discovery.MethodMatcher.matches;

@Stateless
@Local( ServiceHandler.class )
public class CasualServiceHandler implements ServiceHandler
{
    private static final Logger LOG = Logger.getLogger(CasualServiceHandler.class.getName());

    private Context context;

    @Override
    public boolean canHandleService(String serviceName)
    {
        return CasualServiceRegistry.getInstance().hasServiceMetaData( serviceName );
    }

    @Override
    public boolean isServiceAvailable(String serviceName)
    {
        CasualServiceEntry entry = CasualServiceRegistry.getInstance().getServiceEntry( serviceName );
        if( entry == null )
        {
            return false;
        }
        String jndiName = entry.getJndiName();
        try
        {
            loadService( jndiName );
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
        CasualServiceEntry entry = CasualServiceRegistry.getInstance().getServiceEntry( request.getServiceName() );
        ThreadClassLoaderTool tool = new ThreadClassLoaderTool();
        CasualBuffer payload = ServiceBuffer.of( request.getBuffer().getType(), new ArrayList<>() );
        InboundResponse.Builder responseBuilder = InboundResponse.createBuilder();
        try
        {
            Object r = loadService(entry.getJndiName() );
            BufferHandler bufferHandler = BufferHandlerFactory.getHandler( payload.getType() );
            tool.loadClassLoader( r );
            return callService( r, entry, request, bufferHandler );
        }
        catch( Throwable e )
        {
            LOG.log( Level.WARNING, e, ()-> "Error invoking fielded: " + e.getMessage() );
            responseBuilder
                    .errorState(ErrorState.TPESVCERR)
                    .transactionState( TransactionState.ROLLBACK_ONLY );
        }
        finally
        {
            tool.revertClassLoader();
        }

        return responseBuilder.buffer(payload).build();

    }

    @Override
    public ServiceInfo getServiceInfo(String serviceName)
    {
        CasualServiceMetaData entry = CasualServiceRegistry.getInstance().getServiceMetaData( serviceName );

        if( entry == null )
        {
            throw new HandlerException("Service could not be found, should control with canHandle() first." );
        }
        TransactionAttributeType attributeType = TransactionAttributeTypeFinder.find( entry );
        TransactionType transactionType = TransactionTypeMapper.map( attributeType );
        return ServiceInfo.of( entry.getServiceName(), entry.getServiceCategory(), transactionType );
    }

    private Object loadService( String jndiName ) throws NamingException
    {
        Context c = getContext();
        Object r = c.lookup( jndiName );
        LOG.finest( ()->"Found " + r.getClass() + " : " + r );
        return r;
    }

    private InboundResponse callService(Object r, CasualServiceEntry entry, InboundRequest request, BufferHandler bufferHandler ) throws Throwable
    {
        Proxy p = (Proxy)r;

        ServiceCallInfo info = bufferHandler.fromRequest( p, entry.getProxyMethod(), request );

        Method method = info.getMethod().orElseThrow( ()-> new HandlerException( "Buffer did not provide required details about the method end point." ) );

        Object result;
        try
        {
            result = method.invoke( p, info.getParams() );
        }
        catch( IllegalArgumentException e )
        {
            result = retryCallService( p, entry, request, bufferHandler );
        }

        return bufferHandler.toResponse( result );
    }

    /**
     * Issue with weblogic classloaders meaning the method does not match the one we found during service discovery.
     * Once the correct method is found it is saved back into the entry from the service registry so that next
     * time this service is called the NoSuchMethodException will not occur again.
     * NoSuchMethodException never happens in wildfly so this should only be called in weblogic once for first invocation of the method.
     */
    private Object retryCallService(Proxy p, CasualServiceEntry entry, InboundRequest request, BufferHandler bufferHandler ) throws Throwable
    {
        InvocationHandler handler = Proxy.getInvocationHandler( p );
        Method method = entry.getProxyMethod();
        Method proxyMethod = Arrays.stream(p.getClass().getDeclaredMethods())
                .filter( m-> matches( m, method ) )
                .findFirst()
                .orElseThrow( () -> new NoSuchMethodException( "Unable to find method in proxy matching: " + method ));
        entry.setProxyMethod( proxyMethod );
        ServiceCallInfo serviceCallInfo = bufferHandler.fromRequest( p, proxyMethod, request );

        Method m = serviceCallInfo.getMethod().orElseThrow( ()-> new HandlerException( "Buffer did not provided required details about the method end point." ) );

        return handler.invoke( p, m, serviceCallInfo.getParams() );
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
