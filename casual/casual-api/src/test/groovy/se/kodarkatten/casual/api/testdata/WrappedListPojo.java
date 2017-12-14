package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class WrappedListPojo implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(lengthName = "FLD_LONG2")
    private final List<SimplePojo> simplePojos;
    private WrappedListPojo(final List<SimplePojo> simplePojos)
    {
        this.simplePojos = simplePojos;
    }
    public static WrappedListPojo of(final List<SimplePojo> simplePojos)
    {
        Objects.requireNonNull(simplePojos);
        return new WrappedListPojo(simplePojos.stream().collect(Collectors.toList()));
    }
    public List<SimplePojo> getSimplePojos()
    {
        return simplePojos;
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
        WrappedListPojo that = (WrappedListPojo) o;
        return Objects.equals(simplePojos, that.simplePojos);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(simplePojos);
    }
}
