package se.kodarkatten.casual.network.messages;

public final class CasualNWMessage
{
    private final CasualNWMessageType type;
  //  CasualNWMessageHeader header;
  //  CasualNWMessagePayload payload;

    public CasualNWMessage(int id ) {
        type = CasualNWMessageType.unmarshal(id);
    }

    public CasualNWMessageType getType()
    {
        return type;
    }

}
