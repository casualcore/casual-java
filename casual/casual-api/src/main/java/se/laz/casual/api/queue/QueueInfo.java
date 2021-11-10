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

    public static Builder createBuilder()
    {
        return new Builder();
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

    public static final class Builder
    {
        private String qname;
        private QueueOptions options = QueueOptions.defaultOptions();

        public Builder withQueueName(String qname)
        {
            this.qname = qname;
            return this;
        }

        public Builder withOptions(QueueOptions options)
        {
            this.options = options;
            return this;
        }

        public QueueInfo build()
        {
            Objects.requireNonNull(qname, "qname can not be null");
            return new QueueInfo(qname, options);
        }
    }
}
