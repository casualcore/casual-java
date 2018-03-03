/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded

import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedMarshallingException
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessorMode
import se.laz.casual.api.buffer.type.fielded.marshalling.FieldedUnmarshallingException
import se.laz.casual.api.testdata.*
import spock.lang.Shared
import spock.lang.Specification

import java.lang.reflect.Method
import java.time.LocalDate

class FieldedTypeBufferProcessorTest extends Specification
{
    @Shared
    def name = 'John Doe'
    @Shared
    def age = 42
    @Shared
    SimplePojo simplePojo = SimplePojo.of(name, age)
    @Shared
    WrappedPojo wrappedSimplePojo = WrappedPojo.of(simplePojo, 'burrito')
    @Shared
    SimpleListPojo listSimplePojo = SimpleListPojo.of(['sometimes', 'when', 'you', 'fall', 'you', 'fly'], [1, 2, 3, 4, 5])
    @Shared
    SimpleListPojo emptyListSimplePojo = SimpleListPojo.of([], [1, 2, 3, 42])
    @Shared
    int[] valueArray = [1, 2, 3, 4, 5, 6]
    @Shared
    int[] cats = [1,2,3,4]
    @Shared
    int[] dogs = [20,30,40,50,60,70]
    @Shared
    Long[] wrappedNumbers = [10,11,12,13]
    @Shared
    SimplePojo[] simplePojoArray = [SimplePojo.of('Jane Doe', 22), SimplePojo.of('Tarzan', 36)]
    @Shared
    SimpleArrayPojo arraySimplePojo = SimpleArrayPojo.of(valueArray, wrappedNumbers)
    @Shared
    ArraysSameNamePojo arraysSameNamePojo = ArraysSameNamePojo.of(cats, dogs)
    @Shared
    ArrayWithWrappedPojo arrayWithWrappedPojo = ArrayWithWrappedPojo.of(simplePojoArray)
    @Shared
    PojoWithAnnotatedMethods withAnnotatedMethods = PojoWithAnnotatedMethods.of(age, name, ['070-737373', '0730-808080'], Arrays.asList(valueArray))
    @Shared
    LuckyPhoneBookService luckyPhoneBookService = LuckyPhoneBookService.of(age, name, ['070-737373', '0730-808080'], Arrays.asList(valueArray))
    @Shared
    SimplePojoService simplePojoService = SimplePojoService.of(simplePojo)
    @Shared
    PojoWithAnnotatedMethods emptyWithAnnotatedMethods = PojoWithAnnotatedMethods.of(age, name, ['070-737373', '0730-808080'], [])
    @Shared
    WrappedListPojo wrappedListPojo = WrappedListPojo.of(Arrays.asList(SimplePojo.of('Jane Doe', 39), SimplePojo.of('Tarzan', 32)))
    @Shared
    WrappedListPojo emptyWrappedListPojo = WrappedListPojo.of([])
    @Shared
    WrappedListPojoWithAnnotatedMethods wrappedListPojoWithParameters = WrappedListPojoWithAnnotatedMethods.of(Arrays.asList(SimplePojo.of('Jane Doe', 39), SimplePojo.of('Tarzan', 32)))
    @Shared
    WrappedListPojoWithAnnotatedMethods emptyWrappedListPojoWithParameters = WrappedListPojoWithAnnotatedMethods.of([])
    @Shared
    TwoListsSameName twoListsSameName = TwoListsSameName.of([1, 2, 3, 4], [10, 20, 30, 40, 50])
    @Shared
    TwoListsSameName twoListsSameNameFirstEmpty = TwoListsSameName.of([], [10,20,30,40,50])
    @Shared
    TwoListsSameName twoListsSameNameSecondEmpty = TwoListsSameName.of([1,2,3,4,5,], [])
    @Shared
    PojoWithNullableFields pojoWithNullableFieldsMissingName = PojoWithNullableFields.of(null, 42)
    @Shared
    PojoWithMappableField pojoWithMappableField = PojoWithMappableField.of(LocalDate.of(1972, 1, 1), LocalDate.of(2017, 1, 1))
    @Shared
    PojoWithMappableParam pojoWithMappableParam = PojoWithMappableParam.of(LocalDate.of(1972, 1, 1), LocalDate.of(2017, 1, 1))
    @Shared
    PojoWithMappableFieldList pojoWithMappableFieldList = PojoWithMappableFieldList.of([LocalDate.of(1972, 1, 1), LocalDate.of(2017, 1, 1)])
    @Shared
    PojoWithMappableParamList pojoWithMappableParamList = PojoWithMappableParamList.of([LocalDate.of(1972, 1, 1), LocalDate.of(2017, 1, 1)])
    @Shared
    LocalDate[] localDates = [LocalDate.of(1972, 1, 1), LocalDate.of(2017, 1, 1)]
    @Shared
    PojoWithMappableFieldArray pojoWithMappableFieldArray = PojoWithMappableFieldArray.of(localDates)
    @Shared
    PojoWithMappableParamArray pojoWithMappableParamArray = PojoWithMappableParamArray.of(localDates)

    def 'roundtripping - relaxed'()
    {
        expect:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(instance, FieldedTypeBufferProcessorMode.RELAXED)
        !b.isEmpty()
        def r = FieldedTypeBufferProcessor.unmarshall(b, instanceClass, FieldedTypeBufferProcessorMode.RELAXED)
        r == instance
        !b.isEmpty()
        where:
        instance                           | instanceClass
        twoListsSameName                   | TwoListsSameName.class
        twoListsSameNameFirstEmpty         | TwoListsSameName.class
        twoListsSameNameSecondEmpty        | TwoListsSameName.class
        simplePojo                         | SimplePojo.class
        listSimplePojo                     | SimpleListPojo.class
        emptyListSimplePojo                | SimpleListPojo.class
        arraySimplePojo                    | SimpleArrayPojo.class
        arraysSameNamePojo                 | ArraysSameNamePojo.class
        arrayWithWrappedPojo               | ArrayWithWrappedPojo.class
        wrappedSimplePojo                  | WrappedPojo.class
        withAnnotatedMethods               | PojoWithAnnotatedMethods.class
        emptyWithAnnotatedMethods          | PojoWithAnnotatedMethods.class
        wrappedListPojo                    | WrappedListPojo.class
        emptyWrappedListPojo               | WrappedListPojo.class
        wrappedListPojoWithParameters      | WrappedListPojoWithAnnotatedMethods.class
        emptyWrappedListPojoWithParameters | WrappedListPojoWithAnnotatedMethods.class
        pojoWithNullableFieldsMissingName  | PojoWithNullableFields.class
        pojoWithMappableField              | PojoWithMappableField.class
        pojoWithMappableFieldList          | PojoWithMappableFieldList.class
        pojoWithMappableParam              | PojoWithMappableParam.class
        pojoWithMappableParamList          | PojoWithMappableParamList.class
        pojoWithMappableFieldArray         | PojoWithMappableFieldArray.class
        pojoWithMappableParamArray         | PojoWithMappableParamArray.class
    }

    def 'roundtripping - strict'()
    {
        expect:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(instance, FieldedTypeBufferProcessorMode.STRICT)
        !b.isEmpty()
        def r = FieldedTypeBufferProcessor.unmarshall(b, instanceClass, FieldedTypeBufferProcessorMode.STRICT)
        r == instance
        !b.isEmpty()
        where:
        instance                           | instanceClass
        twoListsSameName                   | TwoListsSameName.class
        twoListsSameNameFirstEmpty         | TwoListsSameName.class
        twoListsSameNameSecondEmpty        | TwoListsSameName.class
        simplePojo                         | SimplePojo.class
        listSimplePojo                     | SimpleListPojo.class
        emptyListSimplePojo                | SimpleListPojo.class
        arraySimplePojo                    | SimpleArrayPojo.class
        arraysSameNamePojo                 | ArraysSameNamePojo.class
        arrayWithWrappedPojo               | ArrayWithWrappedPojo.class
        wrappedSimplePojo                  | WrappedPojo.class
        withAnnotatedMethods               | PojoWithAnnotatedMethods.class
        emptyWithAnnotatedMethods          | PojoWithAnnotatedMethods.class
        wrappedListPojo                    | WrappedListPojo.class
        emptyWrappedListPojo               | WrappedListPojo.class
        wrappedListPojoWithParameters      | WrappedListPojoWithAnnotatedMethods.class
        emptyWrappedListPojoWithParameters | WrappedListPojoWithAnnotatedMethods.class
        pojoWithMappableField              | PojoWithMappableField.class
        pojoWithMappableFieldList          | PojoWithMappableFieldList.class
        pojoWithMappableParam              | PojoWithMappableParam.class
        pojoWithMappableParamList          | PojoWithMappableParamList.class
    }

    def 'strict marshalling and null field value'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(pojoWithNullableFieldsMissingName, FieldedTypeBufferProcessorMode.STRICT)
        then:
        b == null
        def e = thrown(FieldedMarshallingException)
        e.message.contains('strict mode but the value for @CasualFieldElement: ')
    }

    def 'relaxed marshalling and strict unmarshalling'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(pojoWithNullableFieldsMissingName, FieldedTypeBufferProcessorMode.RELAXED)
        def r = FieldedTypeBufferProcessor.unmarshall(b, PojoWithNullableFields.class, FieldedTypeBufferProcessorMode.STRICT)
        then:
        r == null
        def e = thrown(FieldedUnmarshallingException)
        e.message == 'strict mode and missing value for name: FLD_STRING1 with index: 0'
    }

    def 'service unmarshalling annotated parameter methods'()
    {
        setup:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(withAnnotatedMethods, FieldedTypeBufferProcessorMode.STRICT)
        when:
        LuckyPhoneBookService s = invokeAllServiceMethods(b, LuckyPhoneBookService.of())
        then:
        s != null
        s == luckyPhoneBookService
    }

    def 'service unmarshalling non annotated method param'()
    {
        setup:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(simplePojo, FieldedTypeBufferProcessorMode.STRICT)
        when:
        SimplePojoService  s = invokeAllServiceMethods(b, SimplePojoService.of())
        then:
        s != null
        s == simplePojoService
    }

    Object invokeAllServiceMethods(FieldedTypeBuffer b, Object instance)
    {
        for(Method m : instance.getClass().getMethods())
        {
            Object[] instantiatedParameters = FieldedTypeBufferProcessor.unmarshall(b, m)
            if(instantiatedParameters.length > 0)
            {
                m.invoke(instance, instantiatedParameters)
            }
        }
        return instance
    }

}
