package se.kodarkatten.casual.api.buffer.type.fielded.json;

import java.util.List;

public final class CasualFielded
{
    private final List<CasualFieldGroup> groups;
    public CasualFielded(final List<CasualFieldGroup> groups)
    {
        this.groups = groups;
    }
    public List<CasualFieldGroup> getGroups()
    {
        return groups;
    }
}
