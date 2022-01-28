/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class PojoWithAnnotatedMethods implements Serializable
{
    private static final long serialVersionUID = 1;
    private int age;
    private String name;
    private List<String> phoneNumbers;
    private List<Integer> luckyNumbers;
    private PojoWithAnnotatedMethods(int age, String name, List<String> phoneNumbers, List<Integer> luckyNumbers)
    {
        this.age = age;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
        this.luckyNumbers = luckyNumbers;
    }

    // NOP-constructor needed
    private PojoWithAnnotatedMethods()
    {}

    public static PojoWithAnnotatedMethods of(int age, String name, final List<String> phoneNumbers, List<Integer> luckyNumbers)
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(phoneNumbers);
        return new PojoWithAnnotatedMethods(age, name, phoneNumbers.stream().collect(Collectors.toList()), luckyNumbers.stream().collect(Collectors.toList()));
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

    public void setAge(@CasualFieldElement(name = "FLD_LONG1") int age)
    {
        this.age = age;
    }

    public void setName(@CasualFieldElement(name = "FLD_STRING1") String name)
    {
        this.name = name;
    }

    @CasualFieldElement(name = "FLD_STRING2", lengthName = "FLD_LONG3")
    public List<String> getPhoneNumbers()
    {
        return phoneNumbers;
    }
    public void setPhoneNumbers(@CasualFieldElement(name = "FLD_STRING2", lengthName = "FLD_LONG3") List<String> l)
    {
        this.phoneNumbers = l.stream().collect(Collectors.toList());
    }

    @CasualFieldElement(name = "FLD_LONG2", lengthName = "FLD_LONG4")
    public List<Integer> getLuckyNumbers()
    {
        return luckyNumbers;
    }

    public void setLuckyNumbers(@CasualFieldElement(name = "FLD_LONG2", lengthName = "FLD_LONG4") List<Integer> numbers)
    {
        this.luckyNumbers = numbers;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PojoWithAnnotatedMethods that = (PojoWithAnnotatedMethods) o;
        return age == that.age &&
            Objects.equals(name, that.name) &&
            Objects.equals(phoneNumbers, that.phoneNumbers) &&
            Objects.equals(luckyNumbers, that.luckyNumbers);
    }

    @Override
    public int hashCode()
    {

        return Objects.hash(age, name, phoneNumbers, luckyNumbers);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("PojoWithAnnotatedMethods{");
        sb.append("age=").append(age);
        sb.append(", name='").append(name).append('\'');
        sb.append(", phoneNumbers=").append(phoneNumbers);
        sb.append(", luckyNumbers=").append(luckyNumbers);
        sb.append('}');
        return sb.toString();
    }
}
