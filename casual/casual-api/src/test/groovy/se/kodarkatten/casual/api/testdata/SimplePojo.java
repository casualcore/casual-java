package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.util.Objects;

public final class SimplePojo
{
    @CasualFieldElement(name = "FLD_STRING2")
    private final String name;
    @CasualFieldElement(name = "FLD_LONG1")
    private final int age;

    // for reflection instance creation
    private SimplePojo()
    {
        this.name = null;
        this.age = 0;
    }

    private SimplePojo(final String name, int age)
    {
        this.name = name;
        this.age = age;
    }

    public static SimplePojo of(final String name, int age)
    {
        Objects.requireNonNull(name);
        return new SimplePojo(name, age);
    }

    public String getName()
    {
        return name;
    }

    public int getAge()
    {
        return age;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimplePojo that = (SimplePojo) o;
        return age == that.age &&
            Objects.equals(name, that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, age);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimplePojo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", age=").append(age);
        sb.append('}');
        return sb.toString();
    }
}
