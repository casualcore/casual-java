/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.external.json;

import java.io.Reader;

public interface JsonProvider
{
    /**
     * Unmarshall from JSON to type T
     * @param r the JSON reader
     * @param clazz the class
     * @param <T> the type
     * @return an instance of type T
     */
    <T> T fromJson(final Reader r, Class<T> clazz);
    /**
     * Unmarshall from JSON to type T
     * @param s the JSON string
     * @param clazz the class
     * @param <T> the type
     * @return an instance of type T
     */
    <T> T fromJson(final String s, Class<T> clazz);
    /**
     * Unmarshall from JSON to type T
     * @param s the JSON string
     * @param clazz the class
     * @param typeAdapter a type adapter
     * @param <T> the type
     * @return an instance of type T
     */
    <T> T fromJson(final String s, Class<T> clazz, Object typeAdapter );

    /**
     * Marshall to JSON
     * @param object the object
     * @return the JSON string representation
     */
    String toJson( Object object );

}
