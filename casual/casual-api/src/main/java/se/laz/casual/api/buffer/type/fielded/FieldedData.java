/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded;

import java.io.Serializable;

public interface FieldedData<T> extends Serializable
{
    T getData();

    <T> T getData(Class<T> clazz);

    FieldType getType();
}
