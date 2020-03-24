/*
 * Copyright (c) 2017 - 2020, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.flags;

import java.util.Arrays;
import java.util.Optional;

/**
 * The transaction state
 * Created by aleph on 2017-03-21.
 */
public enum TransactionState
{
    TX_ACTIVE(0),
    TIMEOUT_ROLLBACK_ONLY(1),
    ROLLBACK_ONLY(2),
    ERROR(3);

    private final int id;
    TransactionState(int id)
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    public static int marshal(final TransactionState t)
    {
        return t.getId();
    }

    public static final TransactionState unmarshal(int n)
    {
        Optional<TransactionState> t = Arrays.stream(TransactionState.values())
                                             .filter(v -> v.getId() == n)
                                             .findFirst();
        return t.orElseThrow(() -> new IllegalArgumentException("TransactionState:" + n));
    }

}
