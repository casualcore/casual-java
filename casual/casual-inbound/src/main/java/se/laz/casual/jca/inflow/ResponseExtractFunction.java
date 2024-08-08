/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inflow;

import jakarta.resource.spi.work.WorkEvent;

@FunctionalInterface
public interface ResponseExtractFunction
{
    ServiceCallResult extract(WorkEvent event, WorkResponseContext context, boolean isTpNoReply);
}
