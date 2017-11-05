package se.kodarkatten.casual.jca.inflow.work;

import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition;
import se.kodarkatten.casual.api.external.json.JsonProvider;
import se.kodarkatten.casual.api.external.json.impl.GsonJscdTypeAdapter;
import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.TransactionState;
import se.kodarkatten.casual.internal.thread.ThreadClassLoaderTool;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.spi.work.Work;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static se.kodarkatten.casual.api.external.json.JsonProviderFactory.getJsonProvider;

public final class CasualServiceCallWork implements Work
{
    private static Logger log = Logger.getLogger(CasualServiceCallWork.class.getName());

    private final CasualServiceCallRequestMessage message;
    private final SocketChannel channel;
    private final CasualNWMessageHeader header;

    private static final JsonProvider jp = getJsonProvider();

    private Context context;

    public CasualServiceCallWork(CasualNWMessageHeader header, CasualServiceCallRequestMessage message, SocketChannel channel )
    {
        this.header = header;
        this.message = message;
        this.channel = channel;
    }

    public CasualServiceCallRequestMessage getMessage()
    {
        return message;
    }

    public SocketChannel getSocketChannel()
    {
        return channel;
    }

    public CasualNWMessageHeader getHeader()
    {
        return header;
    }

    @Override
    public void release()
    {
        /**
         * Currently no way to stop a service lookup or call.
         * Transaction context with which this Work is started
         * is applied with timeout that lies outside this code.
         */
    }

    @Override
    public void run()
    {
        log.info( "run()." );

        ThreadClassLoaderTool tool = new ThreadClassLoaderTool();

        CasualServiceCallReplyMessage.Builder replyBuilder = CasualServiceCallReplyMessage.createBuilder()
                .setXid( message.getXid() )
                .setExecution( message.getExecution() );

        List<byte[]> serviceResult = new ArrayList<>();
        try
        {
            Object r = loadService();
            tool.loadClassLoader( r );
            Object result = callService( r );
            serviceResult.add( jp.toJson( result ).getBytes( StandardCharsets.UTF_8) );
            replyBuilder
                    .setError( ErrorState.OK )
                    .setTransactionState( TransactionState.TX_ACTIVE );
        }
        catch( Throwable e )
        {
            replyBuilder
                    .setError( ErrorState.TPESVCERR )
                    .setTransactionState( TransactionState.ROLLBACK_ONLY );
            serviceResult.add( jp.toJson( e ).getBytes( StandardCharsets.UTF_8) );
            log.warning( ()->"Error occured whilst calling the service: " + e.getMessage() );
        }
        finally
        {
            tool.revertClassLoader();
            CasualServiceCallReplyMessage reply = replyBuilder
                    .setServiceBuffer(ServiceBuffer.of( message.getServiceBuffer().getType(), serviceResult ) )
                    .build();
            CasualNWMessage<CasualServiceCallReplyMessage> replyMessage = CasualNWMessage.of( header.getCorrelationId(),reply );
            CasualNetworkWriter.write( this.channel, replyMessage );
        }
    }

    private Object loadService( ) throws NamingException
    {
        String jndiName = message.getServiceName();
        Context c = getContext();
        Object r = c.lookup( jndiName );
        log.info( ()->"Found " + r.getClass() + " : " + r );
        return r;
    }

    private Object callService( Object r ) throws Throwable
    {
        List<byte[]> payload = message.getServiceBuffer().getPayload();
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
        log.info( ()-> "Result: " + result );
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
