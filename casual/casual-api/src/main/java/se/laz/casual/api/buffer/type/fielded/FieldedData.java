/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import java.io.Serializable;

/**
 * Interface for FieldedData
 */
public interface FieldedData<T> extends Serializable
{
    /**
     * @return the data
     */
    T getData();

    /**
     * @throws ClassCastException if the class does not match
     * @throws ArithmeticException if the underlying type is Long and a cast to Integer is request and it does not fit
     * @param clazz the class to cast to
     * @param <X> the type
     * @return the data
     */
    <X> X getData(Class<X> clazz);

    /**
     * @return the field type
     */
    FieldType getType();
}
