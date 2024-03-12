/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MessageLoop extends Consumer<Supplier<Boolean>>
{
    void handleMessages();
}
