package se.kodarkatten.casual.jca

import se.kodarkatten.casual.jca.inflow.CasualActivationSpec
import se.kodarkatten.casual.jca.inflow.work.CasualInboundWork
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.BootstrapContext
import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager

class CasualResourceAdapterTest extends Specification
{
    @Shared CasualResourceAdapter instance
    @Shared InetSocketAddress okAddress = new InetSocketAddress(0)

    def setup()
    {
        instance = new CasualResourceAdapter()
    }

    def "GetXAResources"()
    {
        expect:
        instance.getXAResources() == null
    }

    def "toString test."()
    {
        expect:
        instance.toString().contains( "CasualResourceAdapter" )
    }

    def "start initialises WorkManager and XATerminator from context."()
    {
        setup:
        BootstrapContext context = Mock( BootstrapContext )
        WorkManager manager = Mock( WorkManager )
        XATerminator xat = Mock( XATerminator )
        context.getWorkManager() >> manager
        context.getXATerminator() >> xat

        when:
        instance.start( context )

        then:
        instance.getWorkManager() == manager
        instance.getXATerminator() == xat
    }

    def "Activate endpoint."()
    {
        setup:
        BootstrapContext context = Mock(BootstrapContext)
        WorkManager manager = Mock(WorkManager)
        XATerminator xat = Mock(XATerminator)
        context.getWorkManager() >> manager
        context.getXATerminator() >> xat
        instance.start(context)
        MessageEndpointFactory factory = Mock(MessageEndpointFactory)
        CasualActivationSpec spec = new CasualActivationSpec()
        spec.setPort(okAddress.getPort())

        when:
        instance.endpointActivation(factory, spec)

        then:
        1 * manager.startWork(_)
    }

    def "Deactivate endpoint"()
    {
        setup:
        CasualInboundWork work = GroovyMock()
        instance.worker = work
        MessageEndpointFactory factory = Mock(MessageEndpointFactory)
        CasualActivationSpec spec = new CasualActivationSpec()
        spec.setPort(okAddress.getPort())

        when:
        instance.endpointDeactivation( factory, spec )

        then:
        1 * work.release()
    }
}
