package se.kodarkatten.casual.api.buffer.type.fielded.json;

import se.kodarkatten.casual.api.buffer.type.fielded.Constants;
import se.kodarkatten.casual.api.buffer.type.fielded.FieldType;
import se.kodarkatten.casual.api.external.json.JsonProviderFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;

public final class CasualFieldedLookup
{
    static Map<String, CasualField> stringToField = new HashMap<>();
    static Map<Long, CasualField> realIdToField = new HashMap<>();
    private CasualFieldedLookup()
    {}
    static
    {
        CasualFielded fielded = slurpJSON(getSystemFieldedJsonSupplier().orElse(() -> getResource(Constants.CASUAL_FIELD_JSON_EMBEDDED)));
        initializeData(fielded);
        stringToField = Collections.unmodifiableMap(stringToField);
        realIdToField = Collections.unmodifiableMap(realIdToField);
    }

    public static Optional<CasualField> forName(final String name)
    {
        return Optional.ofNullable(stringToField.get(name));
    }

    public static Optional<CasualField> forRealId(long id)
    {
        return Optional.ofNullable(realIdToField.get(id));
    }

    private static CasualFielded slurpJSON(final Supplier<URL> resource)
    {
        try
        {

            return JsonProviderFactory.getJsonProvider().fromJson(new FileReader(new File(resource.get().toURI())), CasualFielded.class);
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
