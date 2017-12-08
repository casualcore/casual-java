package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimpleListPojo
{
    @CasualFieldElement(name = "FLD_STRING1")
    final List<String> strings;

    @CasualFieldElement(name = "FLD_LONG3")
    final List<Integer> numbers;

    private SimpleListPojo()
    {
        strings = null;
        numbers = null;
    }
    private SimpleListPojo(final List<String> strings, final List<Integer> numbers)
    {
        this.strings = strings;
        this.numbers = numbers;
    }
    public static SimpleListPojo of(final List<String> l, final List<Integer> numbers)
    {
        Objects.requireNonNull(l);
        return new SimpleListPojo(l.stream().collect(Collectors.toList()), numbers.stream().collect(Collectors.toList()));
    }

    public List<String> getStrings()
    {
        return strings.stream().collect(Collectors.toList());
    }

    public List<Integer> getNumbers()
    {
        return numbers.stream().collect(Collectors.toList());
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
        SimpleListPojo that = (SimpleListPojo) o;
        return Objects.equals(strings, that.strings) &&
            Objects.equals(numbers, that.numbers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(strings, numbers);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimpleListPojo{");
        sb.append("strings=").append(strings);
        sb.append(", numbers=").append(numbers);
        sb.append('}');
        return sb.toString();
    }
}
