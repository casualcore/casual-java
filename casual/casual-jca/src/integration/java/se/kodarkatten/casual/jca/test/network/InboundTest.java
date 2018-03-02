package se.kodarkatten.casual.jca.test.network;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.CasualBufferType;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition;
import se.kodarkatten.casual.api.buffer.type.JsonBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldedTypeBuffer;
import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor;
import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.ErrorState;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.ServiceReturnState;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.example.service.SimpleObject;
import se.kodarkatten.casual.example.service.order.CasualOrder;
import se.kodarkatten.casual.jca.CasualConnection;
import se.kodarkatten.casual.jca.CasualManagedConnection;
import se.kodarkatten.casual.jca.CasualManagedConnectionFactory;
import se.kodarkatten.casual.jca.CasualResourceAdapter;
import se.kodarkatten.casual.network.protocol.messages.service.ServiceBuffer;
import se.kodarkatten.casual.network.protocol.utils.DummyWorkManager;

import javax.resource.ResourceException;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkContext;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static se.kodarkatten.casual.api.external.json.JsonProviderFactory.getJsonProvider;

public class InboundTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    CasualManagedConnectionFactory managedConnectionFactory;
    CasualManagedConnection managedConnection;
    CasualConnection connection;
    Xid id;

    String host = "192.168.99.100";
    int port = 7772;

    @Before
    public void setup() throws ResourceException, XAException {


        managedConnectionFactory = new CasualManagedConnectionFactory();
        managedConnectionFactory.setHostName( host );
        managedConnectionFactory.setPortNumber( port );

        managedConnection = new CasualManagedConnection( managedConnectionFactory, null );

        id = createXid();

        connection  = (CasualConnection)managedConnection.getConnection( null, null );
        managedConnection.getXAResource().start( id, 0);
        BootstrapContext context = new TestBootstrapContext();
        ResourceAdapter r = new CasualResourceAdapter();
        r.start( context );
        managedConnectionFactory.setResourceAdapter( r );
    }

    @After
    public void tearDown() throws ResourceException {
        if( managedConnection != null )
        {
            managedConnection.cleanup();
            managedConnection.destroy();
        }
    }

    @Test
    public void tpCall_two_phase_commit() throws Exception
    {
        callEcho( );
        twoPhaseCommit();
    }

    @Test
    public void tpCall_echo_rollback() throws Exception
    {
        callEcho( );
        rollback();
    }

    @Test
    public void tpCall_one_phase_commit() throws Exception
    {

        callEcho( );
        onePhaseCommit();
    }

    private void callEcho( )
    {
        String serviceName = "java:global/casual-java-testapp/SimpleService!se.kodarkatten.casual.example.service.ISimpleService";
        
        SimpleObject message = new SimpleObject( "Hello from the call definition." );
        JavaServiceCallDefinition callDef = JavaServiceCallDefinition.of( "echo", message );

        String data = getJsonProvider().toJson( callDef );

        CasualBuffer msg = ServiceBuffer.of(CasualBufferType.JSON_JSCD.getName(), JsonBuffer.of( data ).getBytes() );

        ServiceReturn<CasualBuffer> reply = connection.tpcall(serviceName, msg, Flag.of(AtmiFlags.NOFLAG));

        assertThat(reply.getServiceReturnState(), is(equalTo(ServiceReturnState.TPSUCCESS)));

        JsonBuffer replyBuffer = JsonBuffer.of( reply.getReplyBuffer().getBytes() );

        String s = new String( replyBuffer.getBytes().get( 0 ), StandardCharsets.UTF_8 );

        SimpleObject s1 = getJsonProvider().fromJson( s, SimpleObject.class );

        assertThat( s1, is( equalTo( message ) ) );
    }

    @Test
    public void tpCall_fielded_two_phase_commit() throws Exception
    {
        callTestCreateOrder();
        twoPhaseCommit();
    }

    @Test
    public void tpCall_fielded_rollback() throws Exception
    {
        callTestCreateOrder();
        rollback();
    }

    @Test
    public void tpCall_fielded_one_phase_commit() throws Exception
    {
        callTestCreateOrder();
        onePhaseCommit();
    }

    private void callTestCreateOrder(  )
    {
        String serviceName = "TestCreateOrder";
        CasualOrder message = new CasualOrder( );
        message.setProduct( "New fielded product." );
        FieldedTypeBuffer buffer = FieldedTypeBufferProcessor.marshall( message );

        CasualBuffer msg = buffer;

        ServiceReturn<CasualBuffer> reply = connection.tpcall(serviceName, msg, Flag.of(AtmiFlags.NOFLAG));

        assertThat(reply.getServiceReturnState(), is(equalTo(ServiceReturnState.TPSUCCESS)));

        CasualOrder actual = FieldedTypeBufferProcessor.unmarshall(FieldedTypeBuffer.create(reply.getReplyBuffer().getBytes()), CasualOrder.class);

        assertThat(actual.getId(), is(not(nullValue())));
        assertThat(actual.getVersion(), is(not(nullValue())));
        assertThat(actual.getProduct(), is(equalTo(message.getProduct())));
    }

    @Test
    public void tpCall_invalid_service_rollback() throws Exception
    {
        callInvalidService();
        rollback();
    }

    private void callInvalidService(  )
    {
        String serviceName = "UnknownService";
        CasualOrder message = new CasualOrder( );
        message.setProduct( "New fielded product." );
        FieldedTypeBuffer buffer = FieldedTypeBufferProcessor.marshall( message );

        CasualBuffer msg = buffer;

        ServiceReturn<CasualBuffer> reply = connection.tpcall(serviceName, msg, Flag.of(AtmiFlags.NOFLAG));

        assertThat(reply.getServiceReturnState(), is(equalTo(ServiceReturnState.TPFAIL)));
        assertThat( reply.getErrorState(), is( equalTo( ErrorState.TPENOENT ) ) );
    }

    @Test
    public void tpcallTestEchoBuffer() throws Exception
    {
        callTestEcho();
        onePhaseCommit();
    }

    public void callTestEcho()
    {
        String serviceName = "TestCasualEcho";
        CasualOrder message = new CasualOrder();
        message.setId( 123 );
        message.setVersion( 3 );
        message.setProduct( "this is the details in the product" );
        FieldedTypeBuffer buffer = FieldedTypeBufferProcessor.marshall( message );

        CasualBuffer msg = ServiceBuffer.of( "unknown", buffer.getBytes() );

        ServiceReturn<CasualBuffer> reply = connection.tpcall( serviceName, msg, Flag.of( AtmiFlags.NOFLAG ) );

        assertThat( reply.getServiceReturnState(), is( equalTo( ServiceReturnState.TPSUCCESS) ) );

        CasualOrder actual = FieldedTypeBufferProcessor.unmarshall(FieldedTypeBuffer.create(reply.getReplyBuffer().getBytes()), CasualOrder.class);

        assertThat( actual, is( equalTo( message ) ) );
    }

    @Test
    public void serviceExists() throws Exception
    {
        assertThat( connection.serviceExists("TestCreateOrder"), is( true )  );
        assertThat( connection.serviceExists("TestCasualEcho"), is( true )  );
        assertThat( connection.serviceExists("unknown"), is( false )  );
    }

    private void twoPhaseCommit() throws ResourceException, XAException
    {
        managedConnection.getXAResource().end( id, XAResource.TMSUCCESS );
        int status = managedConnection.getXAResource().prepare( id );
        boolean ok = status == XAReturnCode.XA_RDONLY.getId() || status == XAReturnCode.XA_OK.getId();
        assertThat( ok, is( equalTo( true  ) ) );
        if( status == XAReturnCode.XA_OK.getId() )
        {
            managedConnection.getXAResource().commit(id, false);
        }
    }

    private void onePhaseCommit() throws ResourceException, XAException
    {
        managedConnection.getXAResource().end( id, XAResource.TMSUCCESS );
        managedConnection.getXAResource().commit( id, true );
    }

    private void rollback() throws ResourceException, XAException
    {
        managedConnection.getXAResource().end( id, XAResource.TMFAIL );
        managedConnection.getXAResource().rollback( id );
    }

    private Xid createXid()
    {
        String gid = Integer.toString( ThreadLocalRandom.current().nextInt() );
        String b = Integer.toString( ThreadLocalRandom.current().nextInt() );
        return XID.of(gid.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8), 0);
    }

    public class TestBootstrapContext implements BootstrapContext
    {
        @Override
        public WorkManager getWorkManager()
        {
            return DummyWorkManager.of();
        }

        @Override
        public XATerminator getXATerminator()
        {
            return null;
        }

        @Override
        public Timer createTimer() throws UnavailableException
        {
            return null;
        }

        @Override
        public boolean isContextSupported(Class<? extends WorkContext> workContextClass)
        {
            return false;
        }

        @Override
        public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry()
        {
            return null;
        }
    }

}
