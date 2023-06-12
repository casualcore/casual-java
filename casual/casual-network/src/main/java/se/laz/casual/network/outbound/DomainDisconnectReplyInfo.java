/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.network.outbound;

import se.laz.casual.api.util.PrettyPrinter;

import java.util.Objects;
import java.util.UUID;

public class DomainDisconnectReplyInfo
{
    private final UUID corrid;
    private final UUID execution;

    private DomainDisconnectReplyInfo(UUID corrid, UUID execution)
    {
        this.corrid = corrid;
        this.execution = execution;
    }

    public static DomainDisconnectReplyInfo of(UUID corrid, UUID execution)
    {
        Objects.requireNonNull(corrid, "corrid can not be null");
        Objects.requireNonNull(execution, "execution can not be null");
        return new DomainDisconnectReplyInfo(corrid, execution);
    }

    public UUID getCorrid()
    {
        return corrid;
    }

    public UUID getExecution()
    {
        return execution;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        DomainDisconnectReplyInfo that = (DomainDisconnectReplyInfo) o;
        return Objects.equals(getCorrid(), that.getCorrid()) && Objects.equals(getExecution(), that.getExecution());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCorrid(), getExecution());
    }

    @Override
    public String toString()
    {
        return "DomainDisconnectReplyInfo{" +
                "corrid=" + PrettyPrinter.casualStringify(corrid) +
                ", execution=" + PrettyPrinter.casualStringify(execution) +
                '}';
    }
}
