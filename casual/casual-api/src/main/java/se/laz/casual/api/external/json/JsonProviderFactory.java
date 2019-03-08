/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.external.json;

import se.laz.casual.api.external.json.impl.GsonProvider;

public final class JsonProviderFactory
{
    private JsonProviderFactory()
    {}

    /**
     * Get the default JSON provider
     * @see JsonProvider
     * @return the JSON provider
     */
    public static JsonProvider getJsonProvider()
    {
        return new GsonProvider();
    }
}
