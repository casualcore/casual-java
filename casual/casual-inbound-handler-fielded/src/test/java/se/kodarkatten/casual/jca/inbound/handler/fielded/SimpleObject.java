package se.kodarkatten.casual.jca.inbound.handler.fielded;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.Objects;

public class SimpleObject implements Serializable
{
    private static final long serialVersionUID = 1L;

    @CasualFieldElement(name="FLD_STRING1")
    private String field;

    public static SimpleObject of( String message )
    {
        SimpleObject r = new SimpleObject();
        r.setField( message );
        return r;
    }

    public String getField()
    {
        return field;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public String getMessage( String message )
    {
        return message;
    }

    public Integer getMessage( Integer message )
    {
        return message;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        SimpleObject that = (SimpleObject) o;
        return Objects.equals(field, that.field);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(field);
    }

    @Override
    public String toString()
    {
        return "SimpleObject{" +
                "field='" + field + '\'' +
                '}';
    }
}
