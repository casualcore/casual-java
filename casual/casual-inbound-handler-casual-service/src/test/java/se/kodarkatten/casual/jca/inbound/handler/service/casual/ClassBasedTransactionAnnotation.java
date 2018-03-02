package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualService;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@TransactionAttribute(TransactionAttributeType.NEVER)
public class ClassBasedTransactionAnnotation implements SomeInterface
{
    @CasualService(name="someMethod" )
    @Override
    public void someMethod()
    {

    }
}
