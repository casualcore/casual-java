package se.kodarkatten.casual.jca.inflow.work

import se.kodarkatten.casual.jca.inflow.CasualActivationSpec
import spock.lang.Shared
import spock.lang.Specification

import javax.resource.spi.XATerminator
import javax.resource.spi.endpoint.MessageEndpointFactory
import javax.resource.spi.work.WorkManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class CasualInboundWorkTest extends Specification
{
    @Shared CasualInboundWork instance
    @Shared CasualActivationSpec spec
    @Shared MessageEndpointFactory factory
    @Shared WorkManager workManager
    @Shared XATerminator xaTerminator
    @Shared InetSocketAddress okAddress = new InetSocketAddress(0)

    @Shared ExecutorService executorService
    @Shared Future<?> f

    def setup()
    {
        spec = new CasualActivationSpec()
        spec.setPort( okAddress.getPort() )
        factory = Mock( MessageEndpointFactory )
        workManager = Mock( WorkManager )
        xaTerminator = Mock( XATerminator )

        executorService = Executors.newSingleThreadExecutor()

        instance = new CasualInboundWork( spec, factory, workManager, xaTerminator )

        f = executorService.submit( instance )
    }

    def cleanup()
    {
        instance.release()
        f.get()
        executorService.shutdown()
    }

    def "Getters return values provided during creation."()
    {
        expect:
        instance.getSpec() == spec
        instance.getMessageEndpointFactory() == factory
        instance.getWorkManager() == workManager
        instance.getXaTerminator() == xaTerminator
        instance.getInboundServer().running()
        ! instance.isReleased()
    }

    def "Start and release without accepting."()
    {
        when:
        instance.release()

        then:
        instance.isReleased()
        !instance.getInboundServer().running()
        0 * workManager.startWork(_)
    }
}
