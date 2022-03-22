/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import org.junit.Test;
import java.math.BigDecimal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Testing enabling developers to write null to fielded even though it can not be transported.
 * The effect is instead that default value for the type is serialized.
 * IE null -> "", null -> 0.0f etc etc
 */
public class FieldedNullWriteTest
{
    @Test
    public void testShort()
    {
        String name = "FLD_SHORT1";
        Short value = null;
        Short expectedValue = 0;
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        Short readValue = fb.read(name).getData(Short.class);
        assertEquals(readValue, expectedValue);

        name = "FLD_SHORT2";
        fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        readValue = fb.read(name).getData(Short.class);
        assertEquals(readValue, expectedValue);
    }

    @Test
    public void testLong()
    {
        String name = "FLD_LONG1";
        Long value = null;
        Long expectedValue = 0L;
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        Long readValue = fb.read(name).getData(Long.class);
        assertEquals(readValue, expectedValue);
    }

    @Test
    public void testInteger()
    {
        String name = "FLD_LONG1";
        Integer value = null;
        Integer expectedValue = 0;
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        Integer readValue = fb.read(name).getData(Integer.class);
        assertEquals(readValue, expectedValue);
    }

    @Test
    public void testDouble()
    {
        String name = "FLD_DOUBLE1";
        Double value = null;
        Double expectedValue = 0.0d;
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        Double readValue = fb.read(name).getData(Double.class);
        BigDecimal actualAsBigDecimal = BigDecimal.valueOf(readValue);
        BigDecimal expectedAsBigDecimal = BigDecimal.valueOf(expectedValue);
        assertEquals(expectedAsBigDecimal, actualAsBigDecimal);
    }

    @Test
    public void testFloat()
    {
        String name = "FLD_FLOAT1";
        Float value = null;
        Float expectedValue = 0.0f;
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        Float readValue = fb.read(name).getData(Float.class);
        BigDecimal actualAsBigDecimal = BigDecimal.valueOf(readValue);
        BigDecimal expectedAsBigDecimal = BigDecimal.valueOf(expectedValue);
        assertEquals(expectedAsBigDecimal, actualAsBigDecimal);
    }

    @Test
    public void testString()
    {
        String name = "FLD_STRING1";
        String value = null;
        String expectedValue = "";
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        String readValue = fb.read(name).getData(String.class);
        assertEquals(readValue, expectedValue);
    }

    @Test
    public void testCharacter()
    {
        String name = "FLD_CHAR1";
        Character value = null;
        Character expectedValue = Character.MIN_VALUE;
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        Character readValue = fb.read(name).getData(Character.class);
        assertEquals(readValue, expectedValue);
    }

    @Test
    public void testBinary()
    {
        String name = "FLD_BINARY3";
        byte[] value = null;
        byte[] expectedValue = new byte[0];
        FieldedTypeBuffer writeBuffer = FieldedTypeBuffer.createAllowNullUseDefault();
        FieldedTypeBuffer fb = FieldedTypeBuffer.create(writeBuffer.write(name, value).encode());
        byte[] readValue = fb.read(name).getData(byte[].class);
        assertArrayEquals(readValue, expectedValue);
    }

}
