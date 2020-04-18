package se.laz.casual.network.grpc.inbound;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import se.laz.casual.network.messages.CasualGrpc;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class CasualServer
{
    private static Logger log = Logger.getLogger(CasualServer.class.getName());
    private static final int DEFAULT_SHUTDOWN_TIMEOUT_IN_SECONDS = 30;
    private final RequestDelegate delegate;
    private final int port;
    private Server server;

    private CasualServer(RequestDelegate delegate, int port)
    {
        this.delegate = delegate;
        this.port = port;
    }

    public static CasualServer of(RequestDelegate delegate, int port)
    {
        Objects.requireNonNull(delegate, "delegate can not be null");
        return new CasualServer(delegate, port);
    }

    public void start()
    {
        try
        {
            server = ServerBuilder.forPort(port)
                                  .addService(CasualServerImpl.of(delegate))
                                  .build()
                                  .start();
            log.info("CasualServer started, listening on " + port);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run()
                {
                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                    System.err.println("*** shutting down Casual gRPC server since JVM is shutting down");
                    try
                    {
                        stopServer();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace(System.err);
                    }
                    System.err.println("*** server shut down");
                }
            });
        }
        catch (IOException e)
        {
            throw new CasualServerException("CasualServer failed to start", e);
        }
    }

    public void stopServer() throws InterruptedException
    {
        if(null == server)
        {
            log.info(() -> "casual server not started");
            return;
        }
        server.shutdown().awaitTermination(DEFAULT_SHUTDOWN_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown()
    {
        if(null == server)
        {
            log.info(() -> "casual server not started");
            return;
        }
        try
        {
            server.awaitTermination();
        }
        catch (InterruptedException e)
        {
            throw new CasualServerException("Interrupted while awaiting termination", e);
        }
    }

    static class CasualServerImpl extends CasualGrpc.CasualImplBase
    {
        private final RequestDelegate delegate;
        private CasualServerImpl(RequestDelegate delegate)
        {
            this.delegate = delegate;
        }
        public static CasualServerImpl of(RequestDelegate delegate)
        {
            return new CasualServerImpl(delegate);
        }
        @Override
        public void makeRequest(CasualRequest request, StreamObserver<CasualReply> responseObserver)
        {
            try
            {
                CasualReply reply = delegate.handleRequest(request);
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
            }
            catch(Throwable e)
            {
                // notice:
                // The delegate should not leak exceptions here
                // This is just an extra guard in case that does happen
                StatusRuntimeException statusRuntimeException =  Status.INTERNAL.withDescription(e.toString())
                                                                                .asRuntimeException();
                responseObserver.onError(statusRuntimeException);
            }

        }
    }

    /**
     * Main launches the server from the command line
     * Only used for basic testing
     */
    public static void main(String[] args)
    {
        final RequestDelegate delegate = CommandlineDelegate.of();
        final CasualServer server = CasualServer.of(delegate, 9997);
        server.start();
        server.blockUntilShutdown();
    }

}
