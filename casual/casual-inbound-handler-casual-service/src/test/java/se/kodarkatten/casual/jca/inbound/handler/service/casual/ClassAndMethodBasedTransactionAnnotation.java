package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.services.CasualService;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class ClassAndMethodBasedTransactionAnnotation implements SomeInterface
{
    @CasualService(name="someMethod", jndiName = "" )
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    @Override
    public void someMethod()
    {

    }
}
