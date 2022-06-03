/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.annotation;

import se.laz.casual.api.buffer.type.fielded.mapper.CasualObjectMapper;
import se.laz.casual.api.buffer.type.fielded.mapper.PassThroughMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or, return value and parameter, for marshalling/unmarshalling with a specific name
 * Note, the name is from fielded json so it has to exist there
 *
 * Usage:
 * <pre>{@code
 * class X
 * {
 *     {@literal @}CasualFieldElement(name = 'my-name')
 *     private String city;
 * }
 * }</pre>
 *
 * or
 *
 * <pre>{@code
 * class X
 * {
 *     private String city;
 *
 *     {@literal @}CasualFieldElement(name = 'my-name')
 *     public String getCity()
 *     {
 *         return city;
 *     }
 *     public void setCity({@literal @}CasualFieldElement(name = 'my-name') city)
 *     {
 *         this.city = city;
 *     }
 * }
 * }</pre>
 * Instead of just a String, which is a fielded value, you could use a List of Strings ( or any other fielded type)
 *
 *
 * With X defined as either one above you can do:
 * <pre>{@code
 * class Y
 * {
 *     {@literal @}CasualFieldElement(lengthName ="FLD_LONG1")
 *     List<X> theXs;
 * }
 * }</pre>
 *
 * or
 *
 *<pre>{@code
 * class Y
 * {
 *     private List<X> theXs;
 *
 *     {@literal @}CasualFieldElement(lengthName ="FLD_LONG1")
 *     public List<X> getTheXs()
 *     {
 *        return theXs;
 *     }
 *
 *     public void setTheXs( {@literal @}CasualFieldElement(lengthName ="FLD_LONG1") List<X> theXs)
 *     {
 *        this.theXs = theXs;
 *     }
 * }
 * }</pre>
 * You could also do:
 * <pre>{@code
 * class Y
 * {
 *     {@literal @}CasualFieldElement
 *     private X theX;
 * }
 * }</pre>
 *
 * or
 *
 * <pre>{@code
 * class Y
 * {
 *     private X theX;
 *
 *     {@literal @}CasualFieldElement
 *     public X getTheX()
 *     {
 *         return theX;
 *     }
 *
 *     public setTheX( {@literal @}CasualFieldElement X theX)
 *     {
 *         this.theX = theX;
 *     }
 * }
 * }</pre>
 * Notice that with wrapped POJOs you do not need to supply a name
 * If they are in an array/list you do still do need to supply a lengthName
 *
 * Regarding lists:
 * Interface type should be {@code List<Type>} and you can not make any assumptions of what the actual list type is after unmarshalling
 *
 * For arrays and lists you need to supply a lengthName that will be used for the number of items
 * in the list/array.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface CasualFieldElement
{
    /**
     * A name from your fielded json
     * Note that for int/Integer types you should use a name that maps to long
     * as int/Integer are transported as longs
     * @return name.
     */
    String name() default "";

    /**
     * If the annotation annotates an array or a list
     * The name should be a name from your fielded json
     * @return lengthName.
     */
    String lengthName() default "";

    /**
     * Default pass through mapper
     * That is, if no other mapper is supplied - no mapping will take place
     * @return mapper.
     */
    Class<? extends CasualObjectMapper<? extends Object, ? extends Object>> mapper() default PassThroughMapper.class;

}
