package se.laz.casual.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

public class DebuggingHandler extends ChannelInboundHandlerAdapter
{
    private static final Logger LOG = Logger.getLogger(DebuggingHandler.class.getName());
    // Capture the stack trace at the time this handler (or channel) is created.
    private final Exception allocationStackTrace = new Exception("Channel allocation stack trace");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        // You might log the activation event if needed
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        logStackTrace("channelInactive(being closed): " + ctx.channel() , allocationStackTrace);
        allocationStackTrace.printStackTrace();
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        // This will capture any exception that may lead to channel closure.
        logStackTrace("exceptionCaught" , cause);
        super.exceptionCaught(ctx, cause);
    }

    private void logStackTrace(String msg, Throwable cause)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        LOG.warning(() -> msg + "\n" + sw);
    }

}

