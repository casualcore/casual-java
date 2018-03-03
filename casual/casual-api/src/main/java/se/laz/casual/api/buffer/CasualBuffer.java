/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import java.io.Serializable;
import java.util.List;

/**
 * @author jone
 */
public interface CasualBuffer extends Serializable
{
    String getType();
    List<byte[]> getBytes();
}
