/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.buffer.type.fielded.json;

import se.laz.casual.api.buffer.type.fielded.Constants;
import se.laz.casual.api.buffer.type.fielded.FieldType;
import se.laz.casual.api.external.json.JsonProviderFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Data structure containing the canonical representation
 * of what kind of fielded data is available for use
 * Uses static initialization
 * Set the environment variable {@link Constants#CASUAL_FIELD_TABLE} to point to a json file with your definitions
 * @see Constants
 */
public final class CasualFieldedLookup
{
    private static Map<String, CasualField> stringToField;
    private static Map<Long, CasualField> realIdToField;
    private static URL jsonURL;
    private CasualFieldedLookup()
    {}
    static
    {
        stringToField = new HashMap<>();
        realIdToField = new HashMap<>();
        jsonURL = getSystemFieldedJsonSupplier().orElse(() -> getResource(Constants.CASUAL_FIELD_JSON_EMBEDDED)).get();
        CasualFielded fielded = slurpJSON(jsonURL);
        initializeData(fielded);
        stringToField = Collections.unmodifiableMap(stringToField);
        realIdToField = Collections.unmodifiableMap(realIdToField);
    }

    /**
     * Lookup by name
     * @param name the name
     * @return A casual field if found
     */
    public static Optional<CasualField> forName(final String name)
    {
        return Optional.ofNullable(stringToField.get(name));
    }

    /**
     * Lookup by real id
     * @param id the real id
     * @return A casual field if found
     */
    public static Optional<CasualField> forRealId(long id)
    {
        return Optional.ofNullable(realIdToField.get(id));
    }

    /**
     * The URL to the JSON that is used
     * @return the url
     */
    public static URL getURL()
    {
        return jsonURL;
    }

    /**
     * Get all the available names
     * @return a list of names
     */
    public static List<String> getNames()
    {
        return stringToField.keySet().stream().collect(Collectors.toList());
    }

    private static CasualFielded slurpJSON(final URL resource)
    {
        try
        {
            return JsonProviderFactory.getJsonProvider().fromJson(new FileReader(new File(resource.toURI())), CasualFielded.class);
        }
        catch (FileNotFoundException | URISyntaxException e)
        {
            throw new CasualFieldedLookupException("failed loading fielded json", e);
        }
    }

    private static void initializeData(CasualFielded fielded)
    {
        for(CasualFieldGroup g : fielded.getGroups())
        {
            final long base = g.getBase();
            for(CasualField f : g.getFields())
            {
                f.fieldtype = FieldType.unmarshall(f.type);
                final long realId = calculateRealId(f.id, f.getType().getValue(), base);
                f.realId = realId;
                stringToField.put(f.getName(), f);
                realIdToField.put(realId, f);
            }
        }
    }

    private static long calculateRealId(long id, long typeValue, long base)
    {
        //( `base` + `id` ) + (  CASUAL_FIELD_TYPE_BASE x _field_type_) = real id
        return (base + id) + (typeValue * Constants.CASUAL_FIELD_TYPE_BASE);
    }

    private static Optional<Supplier<URL>> getSystemFieldedJsonSupplier()
    {
        final String envJsonFieldFilename = System.getenv(Constants.CASUAL_FIELD_TABLE);
        final Supplier<URL> s = () -> getSystemResource(System.getenv(Constants.CASUAL_FIELD_TABLE));
        return Optional.ofNullable((null == envJsonFieldFilename) ? null : s);
    }

    // resource shenanigans
    private static URL getResource(final String name)
    {
        ClassLoader cl = CasualFieldedLookup.class.getClassLoader();
        URL url = cl.getResource(name);
        Objects.requireNonNull(url, "Could not find resource: " + name);
        return url;
    }

    private static URL getSystemResource(final String name)
    {
        try
        {
            return Paths.get(name).toUri().toURL();
        }
        catch (MalformedURLException e)
        {
            throw new CasualFieldedLookupException("problem with fielded json by name: " + name);
        }
    }

}
