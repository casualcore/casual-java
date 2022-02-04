/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.testdata;

import se.laz.casual.api.buffer.type.fielded.annotation.CasualFieldElement;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LuckyPhoneBookService
{
    private int age;
    private String name;
    private List<String> phoneNumbers;
    private List<Integer> luckyNumbers;

    private LuckyPhoneBookService(int age, String name, List<String> phoneNumbers, List<Integer> luckyNumbers)
    {
        this.age = age;
        this.name = name;
        this.phoneNumbers = phoneNumbers;
        this.luckyNumbers = luckyNumbers;
    }

    // NOP-constructor needed
    private LuckyPhoneBookService()
    {}

    public static LuckyPhoneBookService of(int age, String name, List<String> phoneNumbers, List<Integer> luckyNumbers)
    {
        Objects.requireNonNull(name);
        Objects.requireNonNull(phoneNumbers);
        Objects.requireNonNull(luckyNumbers);
        return new LuckyPhoneBookService(age, name, phoneNumbers.stream().collect(Collectors.toList()), luckyNumbers.stream().collect(Collectors.toList()));
    }

    public static LuckyPhoneBookService of()
    {
        return new LuckyPhoneBookService(0,"", null, null);
    }

    public void setAge(@CasualFieldElement(name = "FLD_LONG1") int age)
    {
        this.age = age;
    }

    public void setName(@CasualFieldElement(name = "FLD_STRING1") String name)
    {
        this.name = name;
    }

    public void setPhoneNumbers(@CasualFieldElement(name = "FLD_STRING2") List<String> l)
    {
        this.phoneNumbers = l.stream().collect(Collectors.toList());
    }

    public void setLuckyNumbers(@CasualFieldElement(name = "FLD_LONG2", lengthName = "FLD_LONG4") List<Integer> numbers)
    {
        this.luckyNumbers = numbers;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        LuckyPhoneBookService that = (LuckyPhoneBookService) o;
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
        final StringBuilder sb = new StringBuilder("LuckyPhoneBookService{");
        sb.append("age=").append(age);
        sb.append(", name='").append(name).append('\'');
        sb.append(", phoneNumbers=").append(phoneNumbers);
        sb.append(", luckyNumbers=").append(luckyNumbers);
        sb.append('}');
        return sb.toString();
    }
}
