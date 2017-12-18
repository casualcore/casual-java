package se.kodarkatten.casual.api.testdata;

import java.util.Objects;

public final class SimplePojoService
{
    private SimplePojo pojo;
    private SimplePojoService(SimplePojo pojo)
    {
        this.pojo = pojo;
    }
    public static SimplePojoService of(SimplePojo pojo)
    {
        Objects.requireNonNull(pojo);
        return new SimplePojoService(pojo);
    }

    public static SimplePojoService of()
    {
        return new SimplePojoService(null);
    }

    public void setPojo(SimplePojo pojo)
    {
        this.pojo = pojo;
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
        SimplePojoService that = (SimplePojoService) o;
        return Objects.equals(pojo, that.pojo);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(pojo);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SimplePojoService{");
        sb.append("pojo=").append(pojo);
        sb.append('}');
        return sb.toString();
    }
}
