package se.kodarkatten.casual.jca.inbound.handler.fielded;

import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor;
import se.kodarkatten.casual.internal.thread.ThreadClassLoaderTool;
import se.kodarkatten.casual.jca.inbound.handler.CasualHandler;
import se.kodarkatten.casual.jca.inbound.handler.InboundRequest;
import se.kodarkatten.casual.jca.inbound.handler.InboundResponse;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static se.kodarkatten.casual.jca.inbound.handler.fielded.MethodMatcher.matches;

@Stateless
@Local( CasualHandler.class )
public class FieldedHandler implements CasualHandler
{
    private static final Logger LOG = Logger.getLogger(FieldedHandler.class.getName());

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
        LOG.info( ()->"Request received: " + request );
        CasualServiceEntry entry = CasualServiceRegistry.getInstance().getEntry( request.getServiceName() );
        ThreadClassLoaderTool tool = new ThreadClassLoaderTool();
        boolean success = true;
        List<byte[]> payload = new ArrayList<>();
        try
        {
            Object r = loadService(entry.getCasualService().jndiName() );
            tool.loadClassLoader( r );
            Object result = callService( r, entry, request.getPayload() );
            if( result != null )
            {
                FieldedTypeBuffer resultBuffer = FieldedTypeBufferProcessor.marshall(result);
                payload = resultBuffer.getBytes();
            }
        }
        catch( Throwable e )
        {
            LOG.warning(()-> "Error invoking service: " + e.getMessage() );
            success = false;
        }
        finally
        {
            tool.revertClassLoader();
        }

        return InboundResponse.of( success, payload );

    }

    private Object loadService( String jndiName ) throws NamingException
    {
        Context c = getContext();
        Object r = c.lookup( jndiName );
        LOG.info( ()->"Found " + r.getClass() + " : " + r );
        return r;
    }

    private Object callService( Object r, CasualServiceEntry entry, List<byte[]> payload ) throws Throwable
    {
        FieldedTypeBuffer buffer = FieldedTypeBuffer.create( payload );

        Object[] params = FieldedTypeBufferProcessor.unmarshall( buffer, entry.getServiceMethod() );

        Method method = entry.getServiceMethod();

        InvocationHandler handler = Proxy.getInvocationHandler( r );
        try
        {
            return handler.invoke( r, method, params );
        }
        catch( NoSuchMethodException e )
        {
            return retryCallService( r, handler, entry, buffer );
        }
    }

    /**
     * Issue with weblogic classloaders meaning the method does not match the one we found during service discovery.
     * Once the correct method is found it is saved back into the entry from the service registry so that next
     * time this service is called the NoSuchMethodException will not occur again.
     * NoSuchMethodException never happens in wildfly so this should only be called in weblogic once for first invocation of the method.
     */
    private Object retryCallService(Object r, InvocationHandler handler, CasualServiceEntry entry, FieldedTypeBuffer buffer ) throws Throwable
    {
        Method method = entry.getServiceMethod();
        Method proxyMethod = Arrays.stream(r.getClass().getDeclaredMethods())
                .filter( m-> matches( m, method ) )
                .findFirst()
                .orElseThrow( () -> new NoSuchMethodException( "Unable to find method in proxy matching: " + method ));
        entry.setProxyMethod( proxyMethod );
        Object[] params = FieldedTypeBufferProcessor.unmarshall( buffer, proxyMethod );

        return handler.invoke( r, proxyMethod, params );
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
