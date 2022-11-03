/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.api.buffer.CasualBuffer;
import se.laz.casual.api.buffer.ServiceReturn;
import se.laz.casual.api.buffer.type.ServiceBuffer;
import se.laz.casual.api.flags.AtmiFlags;
import se.laz.casual.api.flags.ErrorState;
import se.laz.casual.api.flags.Flag;
import se.laz.casual.api.flags.ServiceReturnState;

import java.util.concurrent.CompletableFuture;

/**
 * Casual caller strategy that does a few things:
 * - Exponential backoff on network errors
 * - What is the cap on waiting for retries? Cap on number of retries?
 * - in what cases should a call never retry?
 * - How does this work? Does it do retries on the failed connection, or does it look for alternatives?
 * - How does it distribute calls? Random, Round-robin? Does it support stickiness?
 * - How does it return failed connection-factories to the pool? Manual attempts when none is available,
 */

public class TpCallerFailover implements TpCaller
{
    private static final FailoverAlgorithm algorithm = new FailoverAlgorithm();

    @Override
    public ServiceReturn<CasualBuffer> tpcall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, ConnectionFactoryLookup lookup)
    {
        return algorithm.tpcallWithFailover(
                serviceName,
                lookup,
                // How to call service
                con -> con.tpcall(serviceName, data, flags),
                // What to do if the cache has no entries
                this::tpenoentReply
        );
    }

    @Override
    public CompletableFuture<ServiceReturn<CasualBuffer>> tpacall(String serviceName, CasualBuffer data, Flag<AtmiFlags> flags, ConnectionFactoryLookup lookup)
    {
        return algorithm.tpacallWithFailover(
                serviceName,
                lookup,
                // How to call service
                con -> con.tpacall(serviceName, data, flags),
                // What to do if the cache has no entries
                () -> CompletableFuture.supplyAsync(this::tpenoentReply)
        );
    }

    private ServiceReturn<CasualBuffer> tpenoentReply()
    {
        return new ServiceReturn<>(ServiceBuffer.empty(), ServiceReturnState.TPFAIL, ErrorState.TPENOENT, 0L);
    }
}
