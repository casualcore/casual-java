package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WrappedListPojoWithAnnotatedMethods implements Serializable
{
    private static final long serialVersionUID = 1;
    private List<SimplePojo> simplePojos;
    private WrappedListPojoWithAnnotatedMethods(final List<SimplePojo> simplePojos)
    {
        this.simplePojos = simplePojos;
    }
    public static WrappedListPojoWithAnnotatedMethods of(final List<SimplePojo> simplePojos)
    {
        Objects.requireNonNull(simplePojos);
        return new WrappedListPojoWithAnnotatedMethods(simplePojos.stream().collect(Collectors.toList()));
    }
    @CasualFieldElement(lengthName = "FLD_LONG2")
    public List<SimplePojo> getSimplePojos()
    {
        return simplePojos;
    }

    public void setSimplePojos(@CasualFieldElement(lengthName = "FLD_LONG2") List<SimplePojo> simplePojos)
    {
        this.simplePojos = simplePojos;
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
        WrappedListPojoWithAnnotatedMethods that = (WrappedListPojoWithAnnotatedMethods) o;
        return Objects.equals(simplePojos, that.simplePojos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(simplePojos);
    }
}
