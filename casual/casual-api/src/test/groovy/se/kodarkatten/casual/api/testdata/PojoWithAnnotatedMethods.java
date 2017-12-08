package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PojoWithAnnotatedMethods
{
    private int age;
    private String name;
    private List<String> phoneNumbers;
    private PojoWithAnnotatedMethods(int age, String name, List<String> phoneNumbers)
    {
        this.age = age;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
    }

    private PojoWithAnnotatedMethods()
    {}

    public static PojoWithAnnotatedMethods of(int age, String name, final List<String> phoneNumbers)
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(phoneNumbers);
        return new PojoWithAnnotatedMethods(age, name, phoneNumbers.stream().collect(Collectors.toList()));
    }

    @CasualFieldElement(name = "FLD_LONG1")
    public int getAge()
    {
        return age;
    }

    @CasualFieldElement(name = "FLD_STRING1")
    public String getName()
    {
        return name;
    }

    @CasualFieldElement(name = "FLD_STRING2")
    public List<String> getPhoneNumbers()
    {
        return phoneNumbers;
    }

    public void setName(@CasualFieldElement(name = "FLD_STRING1") String name)
    {
        this.name = name;
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
        PojoWithAnnotatedMethods that = (PojoWithAnnotatedMethods) o;
        return age == that.age &&
            Objects.equals(name, that.name) &&
            Objects.equals(phoneNumbers, that.phoneNumbers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(age, name, phoneNumbers);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("PojoWithAnnotatedMethods{");
        sb.append("age=").append(age);
        sb.append(", name='").append(name).append('\'');
        sb.append(", phoneNumbers=").append(phoneNumbers);
        sb.append('}');
        return sb.toString();
    }
}
