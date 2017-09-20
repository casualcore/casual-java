package se.kodarkatten.casual.api

import se.kodarkatten.casual.api.buffer.CasualBuffer
import se.kodarkatten.casual.api.buffer.ServiceReturn
import se.kodarkatten.casual.api.flags.Flag
import se.kodarkatten.casual.api.flags.ServiceReturnState
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
        def reply = api.tpcall("test", new CasualTestBuffer(), new Flag.Builder().build(), CasualTestBuffer.class)
        then:
        reply.getServiceReturnState() == ServiceReturnState.TPSUCCESS
    }

    def "test async call"()
    {
        setup:
        def api = new CasualAPIImpl()
        when:
        def reply = api.tpacall("test", new CasualTestBuffer(), new Flag.Builder().build(), CasualTestBuffer.class)
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
        <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag flags, Class<X> bufferClass)
        {
            try
            {
                CasualBuffer replyData = bufferClass.newInstance()

                ServiceReturn<X> reply = new ServiceReturn<X>(bufferClass.cast(replyData), ServiceReturnState.TPSUCCESS, null)
                return reply
            } catch (Throwable t)
            {
                t.printStackTrace()
                throw new RuntimeException(t)
            }
        }

        @Override
        <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag flags, Class<X> bufferClass)
        {
            try
            {
                CompletableFuture<ServiceReturn<X>> reply = CompletableFuture.supplyAsync(new CasualSupplier<X>(bufferClass))
                return reply
            } catch (Throwable t)
            {
                t.printStackTrace()
                throw new RuntimeException(t)
            }
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

                ServiceReturn<Y> reply = new ServiceReturn<Y>(replyBufferType.cast(replyData), ServiceReturnState.TPSUCCESS, null)
                return reply

            } catch (Throwable t)
            {
                t.printStackTrace()
                throw new RuntimeException(t)
            }
        }
    }

}