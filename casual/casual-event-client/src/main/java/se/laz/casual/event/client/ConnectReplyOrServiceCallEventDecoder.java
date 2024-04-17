/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.event.client;

import se.laz.casual.api.external.json.JsonProviderFactory;
import se.laz.casual.event.ServiceCallEvent;

public class ConnectReplyOrServiceCallEventDecoder
{
    public static Object decode(String json)
    {
        try
        {
            return JsonProviderFactory.getJsonProvider().fromJson(json, ServiceCallEvent.class);
        }
        catch (Exception e)
        {

        }
    }
}
