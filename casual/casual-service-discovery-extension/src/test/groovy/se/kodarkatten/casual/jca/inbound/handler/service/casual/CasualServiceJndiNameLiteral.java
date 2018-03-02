package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualServiceJndiName;

import java.lang.annotation.Annotation;

public class CasualServiceJndiNameLiteral implements CasualServiceJndiName
{
    private String value;

    public CasualServiceJndiNameLiteral( String value )
    {
        this.value = value;
    }

    @Override
    public String value()
    {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType()
    {
        return CasualServiceJndiName.class;
    }
}
