package casual.test.network.client;

import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.io.CasualNetworkWriter;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.domain.CasualDomainDiscoveryRequestMessage;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by aleph on 2017-03-10.
 */
public final class DomainDiscoveryRequestReplyClient
{
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
            makeNIORequest(clientChannel, domainName);
            getNIOReply(clientChannel);
            clientChannel.close();
        }
        catch (IOException e)
        {
            System.err.println("Fail!\n" + e);
            e.printStackTrace();
            System.exit(-1);
        }
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
                                                                                            .build();
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        CasualNetworkWriter.write(channel, msg);
    }

}
