package se.kodarkatten.casual.api.testdata;

import se.kodarkatten.casual.api.buffer.type.fielded.annotation.CasualFieldElement;
import se.kodarkatten.casual.api.util.Pair;

import java.util.Objects;

public class WrappedPojo
{
    @CasualFieldElement(name = "THIS DOES NOT MATTER")
    private final SimplePojo sp;
    @CasualFieldElement(name = "FLD_STRING1")
    private final String symbol;

    // non fielded POJO
    private final Pair<String, String> p = Pair.of("hello", "there");
    private WrappedPojo()
    {
        this.sp = null;
        this.symbol = null;
    }
    private WrappedPojo(final SimplePojo sp, String symbol)
    {
        this.sp = sp;
        this.symbol = symbol;
    }
    public static WrappedPojo of(final SimplePojo sp, String symbol)
    {
        Objects.requireNonNull(sp);
        Objects.requireNonNull(symbol);
        return new WrappedPojo(sp, symbol);
    }

    public SimplePojo getSp()
    {
        return sp;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public Pair<String, String> getP()
    {
        return p;
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
        WrappedPojo that = (WrappedPojo) o;
        return Objects.equals(sp, that.sp) &&
            Objects.equals(symbol, that.symbol) &&
            Objects.equals(p, that.p);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sp, symbol, p);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("WrappedPojo{");
        sb.append("sp=").append(sp);
        sb.append(", symbol='").append(symbol).append('\'');
        sb.append(", p=").append(p);
        sb.append('}');
        return sb.toString();
    }
}
