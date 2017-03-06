package se.kodarkatten.casual.api;

import org.junit.Test;
import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.flags.ServiceReturnState;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

/**
 * @author jone
 */
public class CasualServiceApiTest
{
    @Test
    public void synchronousCallTest() throws Exception
    {
        CasualServiceApi api = new CasualAPIImpl();

        ServiceReturn<CasualTestBuffer> reply = api.tpcall("test", new CasualTestBuffer(), new Flag.Builder().build(), CasualTestBuffer.class);

        assertEquals("Checking for successful call", ServiceReturnState.TPSUCCESS, reply.getServiceReturnState());
    }

    @Test
    public void asynchronousCallTest() throws Exception
    {
        CasualServiceApi api = new CasualAPIImpl();

        CompletableFuture<ServiceReturn<CasualTestBuffer>> asyncReply = api.tpacall("test", new CasualTestBuffer(), new Flag.Builder().build(), CasualTestBuffer.class);

        ServiceReturn<CasualTestBuffer> reply = asyncReply.get();

        assertEquals("Checking for successful call", ServiceReturnState.TPSUCCESS, reply.getServiceReturnState());
    }

    public static class CasualTestBuffer implements CasualBuffer
    {

    }

    public static class CasualAPIImpl implements CasualServiceApi
    {
        @Override
        public <X extends CasualBuffer> ServiceReturn<X> tpcall(String serviceName, X data, Flag flags, Class<X> bufferClass)
        {
            try
            {
                CasualBuffer replyData = bufferClass.newInstance();

                ServiceReturn<X> reply = new ServiceReturn<X>(bufferClass.cast(replyData), ServiceReturnState.TPSUCCESS, null);
                return reply;
            } catch (Throwable t)
            {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }

        @Override
        public <X extends CasualBuffer> CompletableFuture<ServiceReturn<X>> tpacall(String serviceName, X data, Flag flags, Class<X> bufferClass)
        {
            try
            {
                CompletableFuture<ServiceReturn<X>> reply = CompletableFuture.supplyAsync(new CasualSupplier<X>(bufferClass));
                return reply;
            } catch (Throwable t)
            {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }
    }

    public static class CasualSupplier<Y extends CasualBuffer> implements Supplier<ServiceReturn<Y>>
    {


        private final Class<Y> replyBufferType;

        CasualSupplier(Class<Y> replyBufferType)
        {
            this.replyBufferType = replyBufferType;
        }


        @Override
        public ServiceReturn<Y> get()
        {
            try
            {
                CasualBuffer replyData = replyBufferType.newInstance();

                ServiceReturn<Y> reply = new ServiceReturn<Y>(replyBufferType.cast(replyData), ServiceReturnState.TPSUCCESS, null);
                return reply;

            } catch (Throwable t)
            {
                t.printStackTrace();
                throw new RuntimeException(t);
            }
        }
    }

}
