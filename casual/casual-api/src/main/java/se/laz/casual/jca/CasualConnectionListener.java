package se.laz.casual.jca;

public interface CasualConnectionListener
{
    void newConnection(DomainId domainId);
    void connectionGone(DomainId domainId);

    boolean equals(Object o);
    int hashCode();
}
