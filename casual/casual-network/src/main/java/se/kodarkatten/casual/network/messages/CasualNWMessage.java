package se.kodarkatten.casual.network.messages;

public final class CasualNWMessage
{
    public CasualNWMessageType getType()
    {
        return type;
    }

    private final CasualNWMessageType type;
  //  CasualNWMessageHeader header;
  //  CasualNWMessagePayload payload;

    public CasualNWMessage(int id ) {
        type = CasualNWMessageType.unmarshal(id);
    }

}
