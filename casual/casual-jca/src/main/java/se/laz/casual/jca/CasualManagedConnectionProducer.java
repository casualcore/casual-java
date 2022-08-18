package se.laz.casual.jca;

public interface CasualManagedConnectionProducer
{
    CasualManagedConnection createManagedConnection(CasualManagedConnectionFactory mcf);
}
