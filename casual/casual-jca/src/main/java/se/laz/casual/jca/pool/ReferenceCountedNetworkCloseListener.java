package se.laz.casual.jca.pool;

public interface ReferenceCountedNetworkCloseListener
{
    void closed(ReferenceCountedNetworkConnection networkConnection);
}
