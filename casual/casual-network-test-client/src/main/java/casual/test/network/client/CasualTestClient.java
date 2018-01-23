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
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * Created by aleph on 2017-03-10.
 */
public final class CasualTestClient
{
    private static final String sEchoService = "casual.example.echo";
    private static AsynchronousSocketChannel clientChannel;
    private static AsynchronousChannelGroup channelGroup;

    public static void main(String... args)
    {
        final String host = "127.0.0.1";
        final int port = 7771;
        final String domainName = "casual-java-standalone-client";
        requestResponseNIO2Style(host, port, domainName);
    }

    private static void requestResponseNIO2Style(String host, int port, String domainName)
    {
        try
        {
            channelGroup = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(4));
            clientChannel = AsynchronousSocketChannel.open(channelGroup);
            clientChannel.connect(new InetSocketAddress(host, port));
            makeDomainDiscoveryRequest(clientChannel, domainName);
            CasualNWMessage<CasualDomainDiscoveryReplyMessage> domainDiscoveryReplyMsg = CasualNetworkReader.read(clientChannel);
            System.out.println("Domain discovery reply msg: " + domainDiscoveryReplyMsg);
            makeServiceCall(clientChannel);
            System.out.println("Service call made");
            System.out.println("About to read service call reply ");
            CasualNWMessage<CasualServiceCallReplyMessage> serviceCallReplyMsg = CasualNetworkReader.read(clientChannel);
            System.out.println("Service call reply msg: " + serviceCallReplyMsg);
            CasualServiceCallReplyMessage replyMsg = serviceCallReplyMsg.getMessage();
            System.out.println("Reply service buffer payload: " + new String(replyMsg.getServiceBuffer().getPayload().get(0), StandardCharsets.UTF_8));
            clientChannel.close();
            channelGroup.shutdown();
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
        Flag<AtmiFlags> flags = Flag.of(AtmiFlags.TPNOTRAN);
        CasualServiceCallRequestMessage requestMsg = CasualServiceCallRequestMessage.createBuilder()
                                                                                    .setExecution(UUID.randomUUID())
                                                                                    .setServiceName(sEchoService)
                                                                                    .setXid(XID.NULL_XID)
                                                                                    .setXatmiFlags(flags)
                                                                                    .setServiceBuffer(buffer)
                                                                                    .build();
        CasualNWMessage<CasualServiceCallRequestMessage> msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
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
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
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
        CasualNWMessage<CasualDomainDiscoveryRequestMessage> msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        CasualNetworkWriter.write(channel, msg);
    }

}
