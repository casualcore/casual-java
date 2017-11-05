package se.kodarkatten.casual.api.buffer.type

import se.kodarkatten.casual.api.buffer.type.fielded.FieldType
import se.kodarkatten.casual.api.buffer.type.fielded.json.CasualField
import se.kodarkatten.casual.api.external.json.JsonProvider
import se.kodarkatten.casual.api.external.json.JsonProviderFactory
import se.kodarkatten.casual.api.external.json.impl.GsonJscdTypeAdapter
import spock.lang.Shared
import spock.lang.Specification

class JavaServiceCallDefinitionTest extends Specification
{

    @Shared JavaServiceCallDefinition instance
    @Shared JsonProvider p

    def setup()
    {
        p = JsonProviderFactory.getJsonProvider()

        String methodname = "methodnamevalue"
        String param1 = "value1"
        CasualField param2 = new CasualField( 12L, "name", FieldType.CASUAL_FIELD_FLOAT )


        instance = JavaServiceCallDefinition.of( methodname, param1, param2 )

    }

    def "Gson serialize and then deserialize with a type adapter."()
    {
        setup:
        String value = p.toJson( instance )

        when:
        JavaServiceCallDefinition actual2 = p.fromJson( value, JavaServiceCallDefinition.class,  new GsonJscdTypeAdapter() )

        then:
        instance == actual2
    }
}
