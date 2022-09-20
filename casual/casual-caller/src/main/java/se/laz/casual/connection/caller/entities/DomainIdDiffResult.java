package se.laz.casual.connection.caller.entities;

import se.laz.casual.jca.DomainId;

import java.util.List;
import java.util.Objects;

public class DomainIdDiffResult
{
    private final List<DomainId> newDomainIds;
    private final List<DomainId> lostDomainIds;
    private DomainIdDiffResult(List<DomainId> newDomainIds, List<DomainId> lostDomainIds)
    {
        this.newDomainIds = newDomainIds;
        this.lostDomainIds = lostDomainIds;
    }

    public static DomainIdDiffResult of(List<DomainId> newDomainIds, List<DomainId> lostDomainIds)
    {
        Objects.requireNonNull(newDomainIds, "newDomainIds can not be null");
        Objects.requireNonNull(lostDomainIds, "lostDomainIds can not be null");
        return new DomainIdDiffResult(newDomainIds, lostDomainIds);
    }

    public List<DomainId> getNewDomainIds()
    {
        return newDomainIds;
    }

    public List<DomainId> getLostDomainIds()
    {
        return lostDomainIds;
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
        DomainIdDiffResult that = (DomainIdDiffResult) o;
        return Objects.equals(getNewDomainIds(), that.getNewDomainIds()) && Objects.equals(getLostDomainIds(), that.getLostDomainIds());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getNewDomainIds(), getLostDomainIds());
    }

    @Override
    public String toString()
    {
        return "DomainDiffResult{" +
                "newDomainIds=" + newDomainIds +
                ", lostDomainIds=" + lostDomainIds +
                '}';
    }
}
