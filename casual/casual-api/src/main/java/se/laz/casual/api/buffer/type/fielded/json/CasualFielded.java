/*
 * Copyright (c) 2017 - 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

import java.util.List;

/**
 * Representation of a fielded JSON object
 */
// java:S6206 - refactor to record, not yet.
@SuppressWarnings( "java:S6206" )
public final class CasualFielded
{
    private final List<CasualFieldGroup> groups;

    /**
     * Create the instance
     * @param groups the groups
     */
    public CasualFielded(final List<CasualFieldGroup> groups)
    {
        this.groups = groups;
    }

    /**
     * Get the groups
     * @return a list with the groups
     */
    public List<CasualFieldGroup> getGroups()
    {
        return groups;
    }
}
