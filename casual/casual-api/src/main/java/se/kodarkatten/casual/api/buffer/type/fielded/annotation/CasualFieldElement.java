package se.kodarkatten.casual.api.buffer.type.fielded.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field or, return value and parameter, for marshalling/unmarshalling with a specific name
 * Note, the name is from fielded json so it has to exist there
 *
 * Usage:
 * <pre>
 * @<code>
 * class X
 * {
 *     @CasualFieldElement(name = 'my-name')
 *     private String city;
 * }
 * </code>
 * </pre>
 *
 * or
 *
 * <pre>
 * @<code>
 * class X
 * {
 *     private String city;
 *
 *     @CasualFieldElement(name = 'my-name')
 *     public String getCity()
 *     {
 *         return city;
 *     }
 *     public void setCity(@CasualFieldElement(name = 'my-name') city)
 *     {
 *         this.city = city;
 *     }
 * }
 * </code>
 * </pre>
 * Instead of just a String, which is a fielded value, you could use a List of Strings ( or any other fielded type)
 *
 *
 * With X defined as either one above you can do:
 * <pre>
 * @<code>
 * class Y
 * {
 *     @CasualFieldElement(lengthName ="FLD_LONG1")
 *     List<X> theXs;
 * }
 *</code>
 * </pre>
 *
 * or
 *
 *<pre>
 * @<code>
 * class Y
 * {
 *     private List<X> theXs;
 *
 *     @CasualFieldElement(lengthName ="FLD_LONG1")
 *     public List<X> getTheXs()
 *     {
 *        return theXs;
 *     }
 *
 *     public void setTheXs( @CasualFieldElement(lengthName ="FLD_LONG1") List<X> theXs)
 *     {
 *        this.theXs = theXs;
 *     }
 * }
 *</code>
 * </pre>
 * You could also do:
 * class Y
 * {
 *     @CasualFieldElement
 *     private X theX;
 * }
 *
 * or
 *
 * <pre>
 * @<code>
 * class Y
 * {
 *     private X theX;
 *
 *     @CasualFieldElement
 *     public X getTheX()
 *     {
 *         return theX;
 *     }
 *
 *     public setTheX(@CasualFieldElement X theX)
 *     {
 *         this.theX = theX;
 *     }
 * }
 *</code>
 * </pre>
 * Notice that with wrapped POJOs you do not need to supply a name
 * If they are in an array/list you do still do need to supply a lengthName
 *
 * Regarding lists:
 * Interface type should be List<Type> and you can not make any assumptions of what the actual list type is after unmarshalling
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
     * @return
     */
    String name() default "";

    /**
     * If the annotation annotates an array or a list
     * The name should be a name from your fielded json
     * @return
     */
    String lengthName() default "";
}
