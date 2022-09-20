/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue;

import java.io.Serializable;
import java.util.Objects;

public final class QueueInfo implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final String qname;
    private final QueueOptions options;
    private QueueInfo(String qname, QueueOptions options)
    {
        this.qname = qname;
        this.options = options;
    }

    public static QueueInfo of(String name)
    {
        return of(name, QueueOptions.defaultOptions());
    }

    public static QueueInfo of(String name, QueueOptions queueOptions)
    {
        Objects.requireNonNull(name, "name can not be null");
        Objects.requireNonNull(queueOptions, "queueOptions can not be null");
        return new QueueInfo(name, queueOptions);
    }

    public String getQueueName()
    {
        return qname;
    }

    public QueueOptions getOptions()
    {
        return options;
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
        QueueInfo queueInfo = (QueueInfo) o;
        return Objects.equals(qname, queueInfo.qname) &&
            Objects.equals(options, queueInfo.options);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(qname, options);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("QueueInfo{");
        sb.append(", qname='").append(qname).append('\'');
        sb.append(", options=").append(options);
        sb.append('}');
        return sb.toString();
    }
}
