/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

import java.util.List;

/**
 * A casual field group - part of the representation for a fielded JSON object
 * @see CasualFielded
 * @see CasualField
 */
public final class CasualFieldGroup
{
    private long base;
    private final List<CasualField> fields;

    /**
     * Create an instance
     * note: base is optional and thus 0 if not specified
     * @param base the base
     * @param fields the fields
     */
    public CasualFieldGroup(long base, final List<CasualField> fields)
    {
        this.base = base;
        this.fields = fields;
    }

    /**
     * Get the base
     * @return the base
     */
    public long getBase()
    {
        return base;
    }

    /**
     * Get the fields
     * @return a list of fields
     */
    public List<CasualField> getFields()
    {
        return fields;
    }
}
