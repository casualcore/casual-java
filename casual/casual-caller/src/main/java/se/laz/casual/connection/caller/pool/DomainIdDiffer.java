package se.laz.casual.connection.caller.pool;

import se.laz.casual.connection.caller.entities.DomainIdDiffResult;
import se.laz.casual.jca.DomainId;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DomainIdDiffer
{

    private final List<DomainId> originalDomainIds;
    private final List<DomainId> newDomainIds;

    private DomainIdDiffer(List<DomainId> originalDomainIds, List<DomainId> newDomainIds)
    {
        this.originalDomainIds = originalDomainIds;
        this.newDomainIds = newDomainIds;
    }

    public static DomainIdDiffer of(List<DomainId> originalDomainIds, List<DomainId> newDomainIds)
    {
        Objects.requireNonNull(originalDomainIds, "originalDomainIds can not be null");
        Objects.requireNonNull(newDomainIds, "newDomainIds can not be null");
        return new DomainIdDiffer(originalDomainIds, newDomainIds);
    }


    public DomainIdDiffResult diff()
    {
        List<DomainId> newIds = newDomainIds.stream()
                                            .filter(id -> !originalDomainIds.contains(id))
                                            .collect(Collectors.toList());
        List<DomainId> lostIds = originalDomainIds.stream()
                                                  .filter(id -> !newDomainIds.contains(id))
                                                  .collect(Collectors.toList());
        return DomainIdDiffResult.of(newIds, lostIds);
    }
}
