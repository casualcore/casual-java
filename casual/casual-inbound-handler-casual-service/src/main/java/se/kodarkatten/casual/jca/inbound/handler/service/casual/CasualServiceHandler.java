package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.service.ServiceInfo;
import se.kodarkatten.casual.api.services.CasualService;
import se.kodarkatten.casual.internal.thread.ThreadClassLoaderTool;
import se.kodarkatten.casual.jca.inbound.handler.HandlerException;
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest;
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse;
import se.kodarkatten.casual.jca.inbound.handler.buffer.BufferHandler;
import se.kodarkatten.casual.jca.inbound.handler.buffer.BufferHandlerFactory;
import se.kodarkatten.casual.jca.inbound.handler.buffer.ServiceCallInfo;
import se.kodarkatten.casual.jca.inbound.handler.service.ServiceHandler;
import se.kodarkatten.casual.network.messages.domain.TransactionType;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

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

import static se.kodarkatten.casual.jca.inbound.handler.service.casual.MethodMatcher.matches;

@Stateless
@Local( ServiceHandler.class )
public class CasualServiceHandler implements ServiceHandler
{
    private static final Logger LOG = Logger.getLogger(CasualServiceHandler.class.getName());

    private Context context;

    @Override
    public boolean canHandleService(String serviceName)
    {
        return CasualServiceRegistry.getInstance().hasEntry( serviceName );
    }

    @Override
    public boolean isServiceAvailable(String serviceName)
    {
        CasualServiceEntry entry = CasualServiceRegistry.getInstance().getEntry( serviceName );
        if( entry == null )
        {
            return false;
        }
        String jndiName = entry.getCasualService().jndiName();
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
        CasualServiceEntry entry = CasualServiceRegistry.getInstance().getEntry( request.getServiceName() );
        ThreadClassLoaderTool tool = new ThreadClassLoaderTool();
        boolean success = true;
        CasualBuffer payload = ServiceBuffer.of( request.getBuffer().getType(), new ArrayList<>() );
        try
        {
            Object r = loadService(entry.getCasualService().jndiName() );
            BufferHandler bufferHandler = BufferHandlerFactory.getHandler( payload.getType() );
            tool.loadClassLoader( r );
            payload = callService( r, entry, request.getBuffer(), bufferHandler );
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
        CasualServiceEntry entry = CasualServiceRegistry.getInstance().getEntry( serviceName );

        if( entry == null )
        {
            throw new HandlerException("Service could not be found, should control with canHandle() first." );
        }
        CasualService c = entry.getCasualService();
        TransactionAttributeType attributeType = TransactionAttributeTypeFinder.find( entry );
        TransactionType transactionType = TransactionTypeMapper.map( attributeType );
        return ServiceInfo.of( c.name(), c.category(), transactionType );
    }

    private Object loadService( String jndiName ) throws NamingException
    {
        Context c = getContext();
        Object r = c.lookup( jndiName );
        LOG.finest( ()->"Found " + r.getClass() + " : " + r );
        return r;
    }

    private CasualBuffer callService( Object r, CasualServiceEntry entry, CasualBuffer payload,BufferHandler bufferHandler ) throws Throwable
    {
        Proxy p = (Proxy)r;

        ServiceCallInfo info = bufferHandler.fromBuffer( p, entry.getServiceMethod(), payload );

        Method method = info.getMethod().orElseThrow( ()-> new HandlerException( "Buffer did not provided required details about the method end point." ) );
        InvocationHandler handler = Proxy.getInvocationHandler( r );
        Object result;
        try
        {
            result = handler.invoke( p, method, info.getParams() );
        }
        catch( NoSuchMethodException e )
        {
            result = retryCallService( p, handler, entry, payload, bufferHandler );
        }

        return bufferHandler.toBuffer( result );
    }

    /**
     * Issue with weblogic classloaders meaning the method does not match the one we found during fielded discovery.
     * Once the correct method is found it is saved back into the entry from the fielded registry so that next
     * time this fielded is called the NoSuchMethodException will not occur again.
     * NoSuchMethodException never happens in wildfly so this should only be called in weblogic once for first invocation of the method.
     */
    private Object retryCallService(Proxy p, InvocationHandler handler, CasualServiceEntry entry, CasualBuffer buffer, BufferHandler bufferHandler ) throws Throwable
    {
        Method method = entry.getServiceMethod();
        Method proxyMethod = Arrays.stream(p.getClass().getDeclaredMethods())
                .filter( m-> matches( m, method ) )
                .findFirst()
                .orElseThrow( () -> new NoSuchMethodException( "Unable to find method in proxy matching: " + method ));
        entry.setProxyMethod( proxyMethod );
        ServiceCallInfo serviceCallInfo = bufferHandler.fromBuffer( p, proxyMethod, buffer );

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
