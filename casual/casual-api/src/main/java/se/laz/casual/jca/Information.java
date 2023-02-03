package se.laz.casual.jca;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Information
{
    private final static String INBOUND_SERVER_STARTED = "INBOUND_SERVER_STARTED";
    private final static Map<String, Boolean> information = new ConcurrentHashMap<>();

    private Information()
    {}

    public static boolean isInboundStarted()
    {
        return Optional.ofNullable(information.containsKey(INBOUND_SERVER_STARTED)).orElse(false);
    }

    public static void setInboundStarted()
    {
        information.put(INBOUND_SERVER_STARTED, true);
    }

}
