/*
 * Copyright (c) 2017 - 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.external.json;

import java.util.ServiceLoader;

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
        ServiceLoader<JsonProvider> loader = ServiceLoader.load(JsonProvider.class);
        if(!loader.iterator().hasNext())
        {
            throw new NoJsonProviderAvailableException("No json provider available!");
        }
        return loader.iterator().next();
    }
}
