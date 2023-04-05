/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.javaee;

import jakarta.ejb.Remote;

@Remote
public interface SimpleService
{
    public String echo(String message);
}
