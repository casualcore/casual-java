package casual.test.network.client;

import se.kodarkatten.casual.network.io.CasualNetworkReader;
import se.kodarkatten.casual.network.messages.CasualNWMessage;
import se.kodarkatten.casual.network.messages.CasualNWMessageHeader;
import se.kodarkatten.casual.network.messages.parseinfo.MessageHeaderSizes;
import se.kodarkatten.casual.network.messages.reply.CasualDomainDiscoveryReplyMessage;
import se.kodarkatten.casual.network.messages.request.CasualDomainDiscoveryRequestMessage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by aleph on 2017-03-10.
 */
public final class DomainDiscoveryRequestReplyClient
{
    private static Socket casualSocket;
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
        //requestReponseSocketStyle(host, port, domainName);
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
        catch (IOException | InterruptedException  | ExecutionException e)
        {
            System.err.println("Fail!\n" + e);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void getNIOReply(AsynchronousSocketChannel clientChannel) throws ExecutionException, InterruptedException
    {
        // always read header first
        final byte[] headerBytes = new byte[MessageHeaderSizes.getHeaderNetworkSize()];
        readFully(clientChannel, ByteBuffer.wrap(headerBytes));
        System.out.println("read header");
        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader(headerBytes);
        System.out.println("header:" + header);
        // the read the actual message
        // note this has to be chunked if payload size is > Integer.MAX_VALUE
        final byte[] msgBytes = new byte[(int)header.getPayloadSize()];
        readFully(clientChannel, ByteBuffer.wrap(msgBytes));
        System.out.println("read message");
        List<byte[]> msgByteList = new ArrayList<>();
        msgByteList.add(msgBytes);
        CasualDomainDiscoveryReplyMessage replyMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(msgByteList);
        System.out.println("msg:" + replyMsg);
    }

    private static void readFully(AsynchronousSocketChannel channel, ByteBuffer buffer) throws ExecutionException, InterruptedException
    {
        final int toRead = buffer.array().length;
        int read = 0;
        while(!(read == toRead))
        {
            Future<Integer> readBytes = channel.read(buffer);
            read += readBytes.get();
        }
    }

    private static void makeNIORequest(AsynchronousSocketChannel channel, String domainName) throws ExecutionException, InterruptedException
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName(domainName)
                                                                                            .setQueueNames(Arrays.asList("queueA1"))
                                                                                            .build();
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        List<byte[]> data = msg.toNetworkBytes();
        for(final byte[] bytes : data)
        {
            final int length = bytes.length;
            int bytesWritten = 0;
            while(!(bytesWritten == length))
            {
                Future<Integer> written = channel.write(ByteBuffer.wrap(bytes));
                bytesWritten += written.get();
            }
        }
    }

    private static void requestReponseSocketStyle(String host, int port, String domainName)
    {
        try
        {
            casualSocket = new Socket(host, port);
            System.out.println("Connected to: " + casualSocket);
            makeRequest(casualSocket, domainName);
            getReply(casualSocket);
            casualSocket.close();
        }
        catch (IOException e)
        {
            System.err.println("Fail!\n" + e);
            System.exit(-1);
        }
    }

    private static void makeRequest(Socket casualSocket, String domainName) throws IOException
    {
        CasualDomainDiscoveryRequestMessage requestMsg = CasualDomainDiscoveryRequestMessage.createBuilder()
                                                                                            .setExecution(UUID.randomUUID())
                                                                                            .setDomainId(UUID.randomUUID())
                                                                                            .setDomainName(domainName)
                                                                                            .setQueueNames(Arrays.asList("queueA1"))
                                                                                            .build();
        CasualNWMessage msg = CasualNWMessage.of(UUID.randomUUID(), requestMsg);
        System.out.println("About to send msg: " + msg);
        DataOutputStream os = new DataOutputStream(casualSocket.getOutputStream());
        System.out.println("got outputstream: " + os);
        for(Object o : msg.toNetworkBytes())
        {
            final byte[] d = (byte[]) o;
            os.write(d);
            System.out.println("Data written");
        }
        os.flush();
    }

    private static void getReply(Socket casualSocket) throws IOException
    {
        DataInputStream is = new DataInputStream(casualSocket.getInputStream());
        System.out.println("got inputstream: " + is);
        // always read header first
        final byte[] headerBytes = new byte[MessageHeaderSizes.getHeaderNetworkSize()];
        is.readFully(headerBytes);
        System.out.println("read headers");
        CasualNWMessageHeader header = CasualNetworkReader.networkHeaderToCasualHeader(headerBytes);
        System.out.println("header:" + header);
        // the read the actual message
        // note this has to be chunked if payload size is > Integer.MAX_VALUE
        final byte[] msgBytes = new byte[(int)header.getPayloadSize()];
        is.readFully(msgBytes);
        System.out.println("read message");
        List<byte[]> msgByteList = new ArrayList<>();
        msgByteList.add(msgBytes);
        CasualDomainDiscoveryReplyMessage replyMsg = CasualNetworkReader.networkDomainDiscoverReplyToCasualDomainDiscoveryReplyMessage(msgByteList);
        System.out.println("msg:" + replyMsg);
    }
}
