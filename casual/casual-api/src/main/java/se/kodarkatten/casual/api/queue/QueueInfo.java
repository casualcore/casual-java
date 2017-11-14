package se.kodarkatten.casual.api.queue;

import java.util.Objects;

public final class QueueInfo
{
    private final String qspace;
    private final String qname;
    private QueueInfo(String qspace, String qname)
    {
        this.qspace = qspace;
        this.qname = qname;
    }

    public static Builder createBuilder()
    {
        return new Builder();
    }

    public String getQspace()
    {
        return qspace;
    }

    public String getQname()
    {
        return qname;
    }

    public String getCompositeName()
    {
        return qspace + ":" + qname;
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
        return Objects.equals(qspace, queueInfo.qspace) &&
            Objects.equals(qname, queueInfo.qname);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(qspace, qname);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("QueueInfo{");
        sb.append("qspace='").append(qspace).append('\'');
        sb.append(", qname='").append(qname).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder
    {
        private String qspace;
        private String qname;

        public Builder withQspace(String qspace)
        {
            this.qspace = qspace;
            return this;
        }

        public Builder withQname(String qname)
        {
            this.qname = qname;
            return this;
        }

        public QueueInfo build()
        {
            Objects.requireNonNull(qspace, "qspace can not be null");
            Objects.requireNonNull(qname, "qname can not be null");
            return new QueueInfo(qspace, qname);
        }
    }
}
