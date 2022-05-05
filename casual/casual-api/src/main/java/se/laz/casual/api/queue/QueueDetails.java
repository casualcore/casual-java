/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.queue;

import java.util.Objects;

public class QueueDetails
{
    private final String name;
    private final long retries;

    public QueueDetails(String name, long retries)
    {
        this.name = name;
        this.retries = retries;
    }

    public static QueueDetails of(String name, long retries)
    {
        Objects.requireNonNull(name, "name can not be null");
        return new QueueDetails(name, retries);
    }

    public String getName()
    {
        return name;
    }

    public long getRetries()
    {
        return retries;
    }

    @Override
    public String toString()
    {
        return "QueueDetails{" +
                "name='" + name + '\'' +
                ", retries=" + retries +
                '}';
    }

}
