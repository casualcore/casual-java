package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.services.CasualService;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@TransactionAttribute(TransactionAttributeType.NEVER)
public class ClassBasedTransactionAnnotation implements SomeInterface
{
    @CasualService(name="someMethod", jndiName = "" )
    @Override
    public void someMethod()
    {

    }
}
