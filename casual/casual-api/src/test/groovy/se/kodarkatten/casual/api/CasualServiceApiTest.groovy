package se.kodarkatten.casual.api

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.ServiceReturn
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.flags.ServiceReturnState
import se.kodarkatten.casual.api.queue.MessageSelector
import se.kodarkatten.casual.api.queue.QueueInfo
import se.kodarkatten.casual.api.queue.QueueMessage
import spock.lang.Specification

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier



/**
 * @author jone
 */
class CasualServiceApiTest extends Specification
{
    def "test synchronousCall"()
    {
        setup:
        def api = new CasualAPIImpl()
        when:
        def reply = api.tpcall("test", new CasualTestBuffer(), new Flag.Builder().build())
        then:
        reply.getServiceReturnState() == ServiceReturnState.TPSUCCESS
    }

    def "test async call"()
    {
        setup:
        def api = new CasualAPIImpl()
        when:
        def reply = api.tpacall("test", new CasualTestBuffer(), new Flag.Builder().build())
        then:
        reply.get().getServiceReturnState() == ServiceReturnState.TPSUCCESS
    }

    class CasualTestBuffer implements CasualBuffer
    {
        @Override
        String getType()
        {
            return null
        }

        @Override
        List<byte[]> getBytes()
        {
            return null
        }
    }

    class CasualAPIImpl implements CasualServiceApi
    {
        @Override
        ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag flags)
        {
            try
            {
                CasualBuffer replyData = CasualTestBuffer.class.newInstance()
                ServiceReturn<CasualBuffer> reply = new ServiceReturn<CasualBuffer>(replyData, ServiceReturnState.TPSUCCESS, null,0)
                return reply
            } catch (Throwable t)
            {
                t.printStackTrace()
                throw new RuntimeException(t)
            }
        }

        @Override
        CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag flags)
        {
            try
            {
                CompletableFuture<ServiceReturn<CasualBuffer>> reply = CompletableFuture.supplyAsync(new CasualSupplier<CasualBuffer>(CasualTestBuffer.class))
                return reply
            } catch (Throwable t)
            {
                t.printStackTrace()
                throw new RuntimeException(t)
            }
        }

        @Override
        boolean serviceExists(String serviceName)
        {
            return false
        }
    }

    class CasualSupplier<Y extends CasualBuffer> implements Supplier<ServiceReturn<Y>>
    {


        private final Class<Y> replyBufferType

        CasualSupplier(Class<Y> replyBufferType)
        {
            this.replyBufferType = replyBufferType
        }


        @Override
        ServiceReturn<Y> get()
        {
            try
            {
                CasualBuffer replyData = replyBufferType.newInstance()

                ServiceReturn<Y> reply = new ServiceReturn<Y>(replyBufferType.cast(replyData), ServiceReturnState.TPSUCCESS, null, 0)
                return reply

            } catch (Throwable t)
            {
                t.printStackTrace()
                throw new RuntimeException(t)
            }
        }
    }

}
