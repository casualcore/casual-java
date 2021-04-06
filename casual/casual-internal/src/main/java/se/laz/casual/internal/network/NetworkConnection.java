/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.internal.network;

import se.laz.casual.network.messages.CasualReply;
import se.laz.casual.network.messages.CasualRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Created by aleph on 2017-06-14.
 */
public interface NetworkConnection
{
    CompletableFuture<CasualReply> request(CasualRequest message);
    void close();
    boolean isActive();
}
