/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.event.server;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public interface MessageLoop extends Consumer<BooleanSupplier>
{
    void handleMessages();
}
