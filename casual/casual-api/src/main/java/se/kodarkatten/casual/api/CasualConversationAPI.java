package se.kodarkatten.casual.api;

/**
 * @author jone
 */
public interface CasualConversationAPI
{
    void tpconnect();
    void tpdiscon();
    void tprecv();

    void tpreturn();

    void tpsend();
}
