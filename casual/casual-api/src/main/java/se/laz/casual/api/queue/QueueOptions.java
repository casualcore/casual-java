/*
 * Copyright (c) 2017 - 2019, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue;

import java.io.Serializable;
import java.util.Objects;

public final class QueueOptions implements Serializable
{
    private static final long serialVersionUID = 1L;
    private boolean blocking;
    private QueueOptions()
    {}

    public boolean isBlocking()
    {
        return blocking;
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
        QueueOptions that = (QueueOptions) o;
        return blocking == that.blocking;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(blocking);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("QueueOptions{");
        sb.append("blocking=").append(blocking);
        sb.append('}');
        return sb.toString();
    }

    public static QueueOptions defaultOptions()
    {
        return new QueueOptions();
    }

    public static QueueOptionsBuilder createBuilder()
    {
        return new QueueOptionsBuilder();
    }

    public static final class QueueOptionsBuilder
    {
        private boolean blocking;

        private QueueOptionsBuilder()
        {
        }

        public QueueOptionsBuilder withBlock(boolean block)
        {
            this.blocking = block;
            return this;
        }

        public QueueOptions build()
        {
            QueueOptions queueOptions = new QueueOptions();
            queueOptions.blocking = this.blocking;
            return queueOptions;
        }
    }
}
