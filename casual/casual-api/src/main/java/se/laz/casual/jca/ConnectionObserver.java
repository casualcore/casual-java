package se.laz.casual.jca;

public interface ConnectionObserver
{
    /**
     * The connection is gone and can not be used after this
     * @param domainId
     */
    void connectionGone(DomainId domainId);
}
