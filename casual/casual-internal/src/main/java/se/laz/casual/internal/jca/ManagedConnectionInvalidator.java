/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal.jca;

@FunctionalInterface
public interface ManagedConnectionInvalidator
{
    void invalidate(Exception e);
}
