package se.kodarkatten.casual.jca.service.work;

import se.kodarkatten.casual.api.buffer.CasualBuffer;
import se.kodarkatten.casual.api.buffer.ServiceReturn;

import javax.resource.spi.work.Work;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class FutureServiceReturnWork<X extends CasualBuffer>  implements Work
{
    private final CompletableFuture<ServiceReturn<X>> f;
    private final Supplier<ServiceReturn<X>> supplier;

    private FutureServiceReturnWork(CompletableFuture<ServiceReturn<X>> f, Supplier<ServiceReturn<X>> supplier)
    {
        this.f = f;
        this.supplier = supplier;
    }

    @Override
    public void release()
    {
        // This is a hint that we should complete asap
        // however we have no way to hurry up completion
        // we could cancel the future but not sure if that's what we should do?
    }

    @Override
    public void run()
    {
        try
        {
            f.complete(supplier.get());
        }
        catch(Exception e)
        {
            f.completeExceptionally(e);
        }
    }

    public static <X extends CasualBuffer> Work of(CompletableFuture<ServiceReturn<X>> f, Supplier<ServiceReturn<X>> supplier)
    {
        Objects.requireNonNull(f, "future is not allowed to be null");
        Objects.requireNonNull(supplier, "supplier is not allowed to be null");
        return new FutureServiceReturnWork<>(f, supplier);
    }
}
