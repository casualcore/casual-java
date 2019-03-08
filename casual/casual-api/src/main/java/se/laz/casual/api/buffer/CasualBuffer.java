/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer;

import java.io.Serializable;
import java.util.List;

/**
 * Interface for the type of buffers than can be sent and received
 * @author jone
 */
public interface CasualBuffer extends Serializable
{
    /**
     * @return the buffer type
     */
    String getType();

    /**
     * @return the payload
     */
    List<byte[]> getBytes();
}
