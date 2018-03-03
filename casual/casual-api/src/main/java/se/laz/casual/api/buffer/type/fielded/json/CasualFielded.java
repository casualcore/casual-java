/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

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
