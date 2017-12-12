package se.kodarkatten.casual.api.buffer.type.fielded

import se.kodarkatten.casual.api.buffer.type.fielded.marshalling.FieldedTypeBufferProcessor
import se.kodarkatten.casual.api.testdata.PojoWithAnnotatedMethods
import se.kodarkatten.casual.api.testdata.SimpleArrayPojo
import se.kodarkatten.casual.api.testdata.SimpleListPojo
import se.kodarkatten.casual.api.testdata.SimplePojo
import se.kodarkatten.casual.api.testdata.WrappedListPojo
import se.kodarkatten.casual.api.testdata.WrappedListPojoWithAnnotatedMethods
import se.kodarkatten.casual.api.testdata.WrappedPojo
import spock.lang.Shared
import spock.lang.Specification

class FieldedTypeBufferProcessorTest extends Specification
{
    @Shared
    def name = 'John Doe'
    @Shared
    def age = 42
    @Shared
    SimplePojo p = SimplePojo.of(name, age)
    @Shared
    WrappedPojo wp = WrappedPojo.of(p, 'burrito')
    @Shared
    SimpleListPojo slp = SimpleListPojo.of(['sometimes', 'when', 'you', 'fall', 'you', 'fly'], [1,2,3,4,5])
    @Shared
    int[] valueArray = [1, 2, 3, 4, 5, 6]
    @Shared
    Long[] wrappedNumbers = [10,11,12,13]
    @Shared
    SimpleArrayPojo sap = SimpleArrayPojo.of(valueArray, wrappedNumbers)
    @Shared
    PojoWithAnnotatedMethods withAnnotatedMethods = PojoWithAnnotatedMethods.of(age, name, ['070-737373', '0730-808080'], Arrays.asList(valueArray))
    @Shared
    WrappedListPojo wlp = WrappedListPojo.of(Arrays.asList(SimplePojo.of('Jane Doe', 39), SimplePojo.of('Tarzan', 32)))
    @Shared
    WrappedListPojoWithAnnotatedMethods wlpam = WrappedListPojoWithAnnotatedMethods.of(Arrays.asList(SimplePojo.of('Jane Doe', 39), SimplePojo.of('Tarzan', 32)))


    def 'marshall simple pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(p)
        then:
        b.read('FLD_STRING2').getData() == name
        b.read('FLD_LONG1').getData() == age
    }

    def 'roundtrip simple pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(p)
        SimplePojo r = FieldedTypeBufferProcessor.unmarshall(b, SimplePojo.class)
        then:
        p == r
    }

    def 'marshall simple list pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(slp)
        then:
        verifyList(b, slp.getStrings())
    }

    def 'roundtrip simple list pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(slp)
        SimpleListPojo r = FieldedTypeBufferProcessor.unmarshall(b, SimpleListPojo.class)
        then:
        slp == r
    }

    def 'marshall simple array pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(sap)
        then:
        verifyArray(b, 'FLD_LONG2', sap.getNumbers())
        verifyArray(b, 'FLD_LONG4', sap.getWrappedNumbers())
    }

    def 'roundtrip simple array pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(sap)
        SimpleArrayPojo r = FieldedTypeBufferProcessor.unmarshall(b, SimpleArrayPojo.class)
        then:
        sap == r
    }

    def 'marshall wrapped pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(wp)
        then:
        b.read('FLD_STRING2').getData() == name
        b.read('FLD_LONG1').getData() == age
    }

    def 'roundtrip wrapped pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(wp)
        WrappedPojo r = FieldedTypeBufferProcessor.unmarshall(b, WrappedPojo.class)
        then:
        wp == r
    }

    def 'marshall pojo with annotated methods'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(withAnnotatedMethods)
        then:
        b.read('FLD_LONG1').getData() == age
        b.read('FLD_STRING1').getData() == name
    }

    def 'roundtrip pojo with annotated method params'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(withAnnotatedMethods)
        PojoWithAnnotatedMethods p = FieldedTypeBufferProcessor.unmarshall(b, PojoWithAnnotatedMethods.class)
        then:
        p == withAnnotatedMethods
    }

    def 'roundtrip wrapped list pojo'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(wlp)
        WrappedListPojo p = FieldedTypeBufferProcessor.unmarshall(b, WrappedListPojo.class)
        then:
        p == wlp
    }

    def 'roundtrip wrapped list pojo with annotated methods'()
    {
        when:
        FieldedTypeBuffer b = FieldedTypeBufferProcessor.marshall(wlpam)
        WrappedListPojoWithAnnotatedMethods p = FieldedTypeBufferProcessor.unmarshall(b, WrappedListPojoWithAnnotatedMethods .class)
        then:
        p == wlpam
    }

    def verifyArray(FieldedTypeBuffer b, name, values)
    {
        for(int i = 0; i < values.length; ++i)
        {
            assert b.read(name, i).getData() == values[i]
        }
        return true
    }

    def verifyList(FieldedTypeBuffer fieldedTypeBuffer, List<String> strings)
    {
        for(int i = 0; i < strings.size(); ++i)
        {
            assert fieldedTypeBuffer.read('FLD_STRING1', i).getData() == strings.get(i)
        }
        return true
    }
}
