package se.laz.casual.jca;

@FunctionalInterface
public interface CasualManagedConnectionProducer
{
    CasualManagedConnection createManagedConnection(CasualManagedConnectionFactory mcf);
}
