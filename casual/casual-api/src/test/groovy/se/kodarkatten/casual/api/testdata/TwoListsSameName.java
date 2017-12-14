package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public final class TwoListsSameName implements Serializable
{
    private static final long serialVersionUID = 1;
    @CasualFieldElement(name = "FLD_LONG1", lengthName = "FLD_LONG3")
    final List<Integer> pandas;
    @CasualFieldElement(name = "FLD_LONG1", lengthName = "FLD_LONG2")
    final List<Integer> numbers;
    private TwoListsSameName(final List<Integer> numbers, final List<Integer> pandas)
    {
        this.numbers = numbers;
        this.pandas = pandas;
    }
    public static TwoListsSameName of(final List<Integer> numbers, final List<Integer> pandas)
    {
        Objects.requireNonNull(numbers);
        Objects.requireNonNull(pandas);
        return new TwoListsSameName(numbers, pandas);
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
        TwoListsSameName that = (TwoListsSameName) o;
        return Objects.equals(numbers, that.numbers) &&
               Objects.equals(pandas, that.pandas);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(numbers, pandas);
    }

    @Override
    public String
    toString()
    {
        final StringBuilder sb = new StringBuilder("TwoListsSameName{");
        sb.append("numbers=").append(numbers);
        sb.append(", pandas=").append(pandas);
        sb.append('}');
        return sb.toString();
    }
}
