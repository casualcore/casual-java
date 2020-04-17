package se.laz.casual.network.grpc.inbound;

import se.laz.casual.network.messages.CasualCommitReply;
import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;

// Only used for basic commandline testing
public final class CommandlineDelegate implements RequestDelegate
{
    private CommandlineDelegate()
    {}
    public static RequestDelegate of()
    {
        return new CommandlineDelegate();
    }
    @Override
    public CasualReply handleRequest(CasualRequest request)
    {
        switch(request.getMessageType())
        {
            case COMMIT_REQUEST:
                return handleCommitRequest(request);
            case PREPARE_REQUEST:
                return handlePrepareRequest(request);
            case ROLLBACK_REQUEST:
                return handleRollbackRequest(request);
            case ENQUEUE_REQUEST:
                return handleEnqueueRequest(request);
            case DEQUEUE_REQUEST:
                return handleDequeueRequest(request);
            case DOMAIN_CONNECT_REQUEST:
                return handelDomainConnectRequest(request);
            case DOMAIN_DISCOVERY_REQUEST:
                return handleDomainDisoveryRequest(request);
            case SERVICE_CALL_REQUEST:
                return handleServiceCallRequest(request);
            case MESSAGE_TYPE_UNSPECIFIED:
            case UNRECOGNIZED:
            default:
                throw new IllegalStateException("Unexpected value: " + request.getMessageType());
        }
    }
    // Basic commandline test methods
    private static CasualReply handleDomainDisoveryRequest(CasualRequest request)

    {
        return null;
    }

    private static CasualReply handelDomainConnectRequest(CasualRequest request)
    {
        return null;
    }

    private static CasualReply handleServiceCallRequest(CasualRequest request)
    {
        return null;
    }

    private static CasualReply handleRollbackRequest(CasualRequest request)
    {
        return null;
    }

    private static CasualReply handleDequeueRequest(CasualRequest request)
    {
        return null;
    }

    private static CasualReply handleEnqueueRequest(CasualRequest request)
    {
        return null;
    }

    private static CasualReply handlePrepareRequest(CasualRequest request)
    {
        return null;
    }

    private static CasualReply handleCommitRequest(CasualRequest request)
    {
        return null;
    }
}
