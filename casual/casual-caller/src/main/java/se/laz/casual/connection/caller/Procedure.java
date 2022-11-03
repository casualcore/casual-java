/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.connection.caller;

/**
 * No arg(s), no return, side effect only - procedure
 */
@FunctionalInterface
public interface Procedure
{
    void apply();
}
