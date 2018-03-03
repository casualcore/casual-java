/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

import java.util.List;

public final class CasualFieldGroup
{
    // note: base is optional and thus 0 if not specified
    private long base;
    private final List<CasualField> fields;

    public CasualFieldGroup(long base, final List<CasualField> fields)
    {
        this.base = base;
        this.fields = fields;
    }

    public long getBase()
    {
        return base;
    }

    public List<CasualField> getFields()
    {
        return fields;
    }
}
