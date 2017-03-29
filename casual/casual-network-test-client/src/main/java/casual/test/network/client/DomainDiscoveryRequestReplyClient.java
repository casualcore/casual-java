package casual.test.network.client;

import se.kodarkatten.casual.api.flags.AtmiFlags;
import se.kodarkatten.casual.api.flags.Flag;
import se.kodarkatten.casual.api.xa.XID;
import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallReplyMessage;
import se.kodarkatten.casual.network.messages.service.CasualServiceCallRequestMessage;
import se.kodarkatten.casual.network.messages.service.ServiceBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-10.
 */
public final class DomainDiscoveryRequestReplyClient
{
    private static final String sEchoService = "casual.example.echo";
    private static AsynchronousSocketChannel clientChannel;

    public static void main(String... args)
    {
        // should be host port domainname
        if(args.length != 3)
        {
            System.err.println("Usage: java DomainDiscoveryRequestReplyClient <host name> <port number> <domain name>");
            System.exit(-1);
        }
        int index = 0;
        final String host = args[index++];
        final int port = Integer.parseInt(args[index++]);
        final String domainName = args[index++];
        requestResponseNIO2Style(host, port, domainName);
    }

    private static void requestResponseNIO2Style(String host, int port, String domainName)
    {
        try
        {
            clientChannel = AsynchronousSocketChannel.open();
            clientChannel.connect(new InetSocketAddress(host, port));
            makeDomainDiscoveryRequest(clientChannel, domainName);
            CasualNWMessage domainDiscoveryReplyMsg = CasualNetworkReader.read(clientChannel);
            System.out.println("Domain discovery reply msg: " + domainDiscoveryReplyMsg);
            makeServiceCall(clientChannel);
            CasualNWMessage serviceCallReplyMsg = CasualNetworkReader.read(clientChannel);
            System.out.println("Service call reply msg: " + serviceCallReplyMsg);
            CasualServiceCallReplyMessage replyMsg = (CasualServiceCallReplyMessage) serviceCallReplyMsg.getMessage();
            System.out.println("Reply service buffer payload: " + new String(replyMsg.getServiceBuffer().getPayload().get(0), StandardCharsets.UTF_8));
            clientChannel.close();
        }
        catch (IOException e)
        {
            System.err.println("Fail!\n" + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void makeServiceCall(final AsynchronousSocketChannel clientChannel)
    {
        String payload = "{\"Hello\": \"world!\"}";
        List<byte[]> msgData = new ArrayList<>();
        msgData.add(payload.getBytes(StandardCharsets.UTF_8));
        ServiceBuffer buffer = ServiceBuffer.of(".json/", msgData);
        Flag<AtmiFlags> flags = new Flag.Builder().build();
        CasualServiceCallRequestMessage requestMsg = CasualServiceCallRequestMessage.createBuilder()
                                                                                    .setExecution(UUID.randomUUID())
                                                                                    .setCallDescriptor(42)
                                                                                    .setServiceName(sEchoService)
                                                                                    .setXid(XID.of())
                                                                                    .setXatmiFlags(flags)
                                                                                    .setServiceBuffer(buffer)
                                                                                    .build();
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        CasualNetworkWriter.write(clientChannel, msg);
    }

    private static void makeDomainDiscoveryRequest(AsynchronousSocketChannel channel, String domainName)
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName(domainName)
                                                                                            .setQueueNames(Arrays.asList("queueA1"))
                                                                                            .setServiceNames(Arrays.asList("casual.example.echo"))
                                                                                            .build();
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        CasualNetworkWriter.write(channel, msg);
    }

    private static void getNIOReply(AsynchronousSocketChannel channel)
    {
        // set to force chunking
        //CasualNetworkReader.setMaxSingleBufferByteSize(1);
        CasualNWMessage<CasualDomainDiscoveryReplyMessage> replyMsg = CasualNetworkReader.read(channel);
        System.out.println("reply msg:" + replyMsg);
    }

    private static void makeNIORequest(AsynchronousSocketChannel channel, String domainName)
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName(domainName)
                                                                                            .setQueueNames(Arrays.asList("queueA1"))
                                                                                            .setServiceNames(Arrays.asList(sEchoService))
                                                                                            .build();
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        CasualNetworkWriter.write(channel, msg);
    }

}
