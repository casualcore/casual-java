package se.kodarkatten.casual.jca.test.network;

import groovy.json.internal.Charsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.buffer.type.JavaServiceCallDefinition;
import se.kodarkatten.casual.api.buffer.type.JsonBuffer;
import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.ServiceReturnState;
import se.kodarkatten.casual.api.xa.XAReturnCode;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.example.service.SimpleObject;
import se.kodarkatten.casual.jca.CasualConnection;
import se.kodarkatten.casual.jca.CasualManagedConnection;
import se.kodarkatten.casual.jca.CasualManagedConnectionFactory;

import javax.resource.ResourceException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static se.kodarkatten.casual.api.external.json.JsonProviderFactory.getJsonProvider;

public class InboundTest
{
    CasualManagedConnectionFactory managedConnectionFactory;
    CasualManagedConnection managedConnection;
    CasualConnection connection;

    String host = "192.168.99.100";
    int port = 7772;

    @Before
    public void setup() throws ResourceException, XAException {
        managedConnectionFactory = new CasualManagedConnectionFactory();
        managedConnectionFactory.setHostName( host );
        managedConnectionFactory.setPortNumber( port );
    }

    @After
    public void tearDown() throws ResourceException {
        if( managedConnection != null )
        {
            managedConnection.destroy();
        }
    }

    @Test
    public void tpCall_prepare_rollback_commit() throws XAException, ResourceException, InterruptedException {
        managedConnection = new CasualManagedConnection( managedConnectionFactory, null );

        //Weblogic
        //String serviceName = "se.kodarkatten.casual.example.service.ISimpleService#se.kodarkatten.casual.example.service.ISimpleService";
        //Wildfly
        String serviceName = "java:jboss/exported/casual-java-testapp/SimpleService!se.kodarkatten.casual.example.service.ISimpleService";
        SimpleObject message = new SimpleObject( "Hello from the call definition." );
        //Integer message = 123;
        JavaServiceCallDefinition callDef = JavaServiceCallDefinition.of( "echo", message );

        for( int i = 0; i<=3; i++ )
        {
            String gid = "1231" + i;
            String b = "3211" + i;
            Xid id = XID.of( gid.getBytes(Charsets.UTF_8), b.getBytes(Charsets.UTF_8), 0 );
            managedConnection.getXAResource().start( id, 0);

            connection  = (CasualConnection)managedConnection.getConnection( null, null );

            String data = getJsonProvider().toJson( callDef );
            CasualBuffer msg = JsonBuffer.of( data );


            ServiceReturn<CasualBuffer> reply = connection.tpcall(serviceName, msg, Flag.of(AtmiFlags.NOFLAG), CasualBuffer.class);

            assertThat(reply.getServiceReturnState(), is(equalTo(ServiceReturnState.TPSUCCESS)));

            JsonBuffer replyBuffer = JsonBuffer.of( reply.getReplyBuffer().getBytes() );

            String s = new String( replyBuffer.getBytes().get( 0 ), StandardCharsets.UTF_8 );

            SimpleObject s1 = getJsonProvider().fromJson( s, SimpleObject.class );

            assertThat( s1, is( equalTo( message ) ) );

            switch( i )
            {
                case 0:
                    int status = managedConnection.getXAResource().prepare( id );
                    boolean ok = status == XAReturnCode.XA_RDONLY.getId() || status == XAReturnCode.XA_OK.getId();
                    assertThat( ok, is( equalTo( true  ) ) );
                    if( status == XAReturnCode.XA_OK.getId() )
                    {
                        managedConnection.getXAResource().commit(id, false);
                    }
                    break;
                case 1:
                    managedConnection.getXAResource().rollback( id );
                    break;
                case 2:
                    managedConnection.getXAResource().commit( id, true );
                    break;
            }
            managedConnection.getXAResource().end( id, XAResource.TMSUCCESS );
            managedConnection.cleanup();
        }
    }
}
