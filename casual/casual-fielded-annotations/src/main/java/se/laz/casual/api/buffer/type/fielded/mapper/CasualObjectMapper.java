/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.mapper;

public interface CasualObjectMapper<S extends Object, D extends Object>
{
    D to(S src);
    S from(D dst);
    Class<?> getDstType();
}