/*
 * Copyright (c) 2024, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.config

import com.github.stefanbirkner.systemlambda.SystemLambda
import spock.lang.Specification

class ConfigurationEnvsReaderTest extends Specification
{
    ConfigurationStore store;

    def setup()
    {
        store = new ConfigurationStore();
    }

    def "With config file set."()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_CONFIG_FILE.getName(  ), value ).execute {
            ConfigurationEnvsReader.populateConfigFileEnv( store )
        }
        String actual = store.get( ConfigurationOptions.CASUAL_CONFIG_FILE )

        then:
        actual == expected

        where:
        value         || expected
        ""            || ""
        "casual.json" || "casual.json"
        null          || ""
    }

    def "With fielded envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING.getName(  ), encoding )
                .and( ConfigurationOptions.CASUAL_FIELD_TABLE.getName(  ), table ).execute {
            ConfigurationEnvsReader.populateStoreFromEnvs( store )
        }
        String actualEncoding = store.get( ConfigurationOptions.CASUAL_API_FIELDED_ENCODING )
        String actualTable = store.get( ConfigurationOptions.CASUAL_FIELD_TABLE )

        then:
        actualEncoding == expectedEncoding
        actualTable == expectedTable

        where:
        encoding | table          || expectedEncoding | expectedTable
        "UTF8"   | "some.json"    || "UTF8"           | "some.json"
        "latin1" | "another.json" || "latin1"         | "another.json"
        ""       | ""             || "UTF8"           | "casual-fields.json"
        null     | null           || "UTF8"           | "casual-fields.json"
    }

    def "With loghander envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER.getName(  ), outbound )
                .and( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER.getName(  ), inbound )
                .and( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER.getName(  ), reverse ).execute {
            ConfigurationEnvsReader.populateStoreFromEnvs( store )
        }
        Boolean actualInbound = store.get( ConfigurationOptions.CASUAL_NETWORK_INBOUND_ENABLE_LOGHANDLER )
        Boolean actualOutbound = store.get( ConfigurationOptions.CASUAL_NETWORK_OUTBOUND_ENABLE_LOGHANDLER )
        Boolean actualReverse = store.get( ConfigurationOptions.CASUAL_NETWORK_REVERSE_INBOUND_ENABLE_LOGHANDLER )

        then:
        actualInbound == expectedInbound
        actualOutbound == expectedOutbound
        actualReverse == expectedReverse

        where:
        inbound | outbound | reverse || expectedInbound | expectedOutbound | expectedReverse
        "false" | "false"  | "true"  || false           | false            | true
        "false" | "true"   | "false" || false           | true             | false
        "true"  | "false"  | "false" || true            | false            | false
        "false" | "false"  | "false" || false           | false            | false
        "true"  | "true"   | "true"  || true            | true             | true
        ""      | ""       | ""      || false           | false            | false
        " "     | " "      | " "     || false           | false            | false
        null    | null     | null    || false           | false            | false
    }

    def "With netty log levels envs"()
    {
        when:
        SystemLambda.withEnvironmentVariable( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL.getName(  ), outbound )
                .and( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL.getName(  ), inbound )
                .and( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL.getName(  ), reverse ).execute {
            ConfigurationEnvsReader.populateStoreFromEnvs( store )
        }
        String actualInbound = store.get( ConfigurationOptions.CASUAL_INBOUND_NETTY_LOGGING_LEVEL )
        String actualOutbound = store.get( ConfigurationOptions.CASUAL_OUTBOUND_NETTY_LOGGING_LEVEL )
        String actualReverse = store.get( ConfigurationOptions.CASUAL_REVERSE_INBOUND_NETTY_LOGGING_LEVEL )

        then:
        actualInbound == expectedInbound
        actualOutbound == expectedOutbound
        actualReverse == expectedReverse

        where:
        inbound | outbound | reverse || expectedInbound | expectedOutbound | expectedReverse
        "ERROR" | "INFO"   | "WARN"  || "ERROR"         | "INFO"           | "WARN"
        "TRACE" | "DEBUG"  | "ERROR" || "TRACE"         | "DEBUG"          | "ERROR"
        "INFO"  | "INFO"   | "INFO"  || "INFO"          | "INFO"           | "INFO"
        ""      | ""       | ""      || "INFO"          | "INFO"           | "INFO"
        " "     | " "      | " "     || "INFO"          | "INFO"           | "INFO"
        null    | null     | null    || "INFO"          | "INFO"           | "INFO"
    }

    //TODO check conversions that are invalid values e.g. boolean with "xyz" or integer with "false".
    //TODO what should we do when there is a value which maps to an enum and the value does not match?
    //TODO netty log level, should that be an enum?

}
