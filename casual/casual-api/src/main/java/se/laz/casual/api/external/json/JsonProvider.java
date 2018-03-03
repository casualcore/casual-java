/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.external.json;

import java.io.Reader;

public interface JsonProvider
{
    <T> T fromJson(final Reader r, Class<T> clazz);
    <T> T fromJson(final String s, Class<T> clazz);
    <T> T fromJson(final String s, Class<T> clazz, Object typeAdapter );
    String toJson( Object object );

}
