/*
 * Copyright (c) 2021 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config.json;

import com.google.gson.annotations.SerializedName;
import se.laz.casual.config.Mode.Constants;

enum Mode
{
    @SerializedName( Constants.IMMEDIATE)
    IMMEDIATE( Constants.IMMEDIATE ),
    @SerializedName(Constants.TRIGGER)
    TRIGGER( Constants.TRIGGER ),
    @SerializedName(Constants.DISCOVER)
    DISCOVER( se.laz.casual.config.Mode.Constants.DISCOVER );

    private final String name;

    Mode( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
