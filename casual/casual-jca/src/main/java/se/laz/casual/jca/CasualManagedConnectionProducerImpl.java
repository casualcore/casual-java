package se.laz.casual.jca;

public class CasualManagedConnectionProducerImpl implements CasualManagedConnectionProducer
{
    @Override
    public CasualManagedConnection createManagedConnection(CasualManagedConnectionFactory mcf)
    {
        return new CasualManagedConnection(mcf);
    }
}
