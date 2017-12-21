package se.kodarkatten.casual.jca.inbound.handler.jndi;

import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition;
import se.kodarkatten.casual.api.external.json.JsonProvider;
import se.kodarkatten.casual.api.external.json.impl.GsonJscdTypeAdapter;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static se.kodarkatten.casual.api.external.json.JsonProviderFactory.getJsonProvider;

@Stateless
@Local( CasualHandler.class )
public class JndiHandler implements CasualHandler
{
    private static final Logger LOG = Logger.getLogger(JndiHandler.class.getName());

    private static final JsonProvider jp = getJsonProvider();

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
        LOG.info( ()->"Request received: " + request );
        ThreadClassLoaderTool tool = new ThreadClassLoaderTool();
        boolean success = true;
        List<byte[]> payload = new ArrayList<>();
        try
        {
            Object r = loadService(request.getServiceName());
            tool.loadClassLoader( r );
            Object result = callService( r, request.getPayload() );
            payload.add( jp.toJson( result ).getBytes( StandardCharsets.UTF_8) );
        }
        catch( Throwable e )
        {
            success = false;
            payload.add( jp.toJson( e ).getBytes( StandardCharsets.UTF_8) );
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

    private Object callService( Object r, List<byte[]> payload ) throws Throwable
    {
        if( payload.size() != 1 )
        {
            throw new IllegalArgumentException( "Payload size must be 1 but was " + payload.size() );
        }

        String s = new String( payload.get( 0 ), StandardCharsets.UTF_8 );
        JavaServiceCallDefinition callDef = jp.fromJson( s, JavaServiceCallDefinition.class, new GsonJscdTypeAdapter() );

        Proxy p = (Proxy) r;
        String[] methodParamTypes = callDef.getMethodParamTypes();
        Class<?>[] params = new Class<?>[methodParamTypes.length];
        for( int i=0; i< methodParamTypes.length; i++ )
        {
            params[i] = Class.forName( methodParamTypes[i], true, Thread.currentThread().getContextClassLoader() );
        }
        Method method = p.getClass().getMethod( callDef.getMethodName(), params );
        InvocationHandler handler = Proxy.getInvocationHandler( r );
        Object result = handler.invoke( r, method, callDef.getMethodParams() );
        LOG.info( ()-> "Result: " + result );
        return result;
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
