/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller.config;

import java.util.Objects;
import java.util.Optional;

public class Configuration
{
    public static final String CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT_ENV_NAME = "CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT";
    public static final String CASUAL_CALLER_VALIDATION_INTERVAL_ENV_NAME = "CASUAL_CALLER_VALIDATION_INTERVAL";
    public static final String CASUAL_CALLER_TRANSACTION_STICKY_ENV_NAME = "CASUAL_CALLER_TRANSACTION_STICKY";

    private String jndiSearchRoot;
    private Integer validationIntervalMillis;
    private Boolean transactionStickyEnabled;

    private static final String DEFAULT_JNDI_SEARCH_ROOT = "eis";
    private static final String DEFAULT_VALIDATION_INTERVAL_MILLIS = "5000";
    private static final String DEFAULT_TRANSACTION_STICKY = "false";

    private Configuration(Builder builder)
    {
        jndiSearchRoot = builder.jndiSearchRoot;
        validationIntervalMillis = builder.validationIntervalMillis;
        transactionStickyEnabled = builder.transactionStickyEnabled;
    }

    public String getJndiSearchRoot()
    {
        if (jndiSearchRoot == null) jndiSearchRoot = getJndiSearchRootFromEnv();
        return jndiSearchRoot;
    }

    public int getValidationIntervalMillis()
    {
        if (validationIntervalMillis == null) validationIntervalMillis = getValidationIntervalMillisFromEnv();
        return validationIntervalMillis;
    }

    public boolean isTransactionStickyEnabled()
    {
        if (transactionStickyEnabled == null) transactionStickyEnabled = isTransactionStickyEnabledFromEnv();
        return transactionStickyEnabled;
    }

    public static Configuration fromEnvOrDefaults()
    {
        return builder()
                .jndiSearchRoot(getJndiSearchRootFromEnv())
                .validationIntervalMillis(getValidationIntervalMillisFromEnv())
                .transactionStickyEnabled(isTransactionStickyEnabledFromEnv())
                .build();
    }

    private static String getJndiSearchRootFromEnv()
    {
        return Optional.ofNullable(System.getenv(CASUAL_CALLER_CONNECTION_FACTORY_JNDI_SEARCH_ROOT_ENV_NAME))
                .orElse(DEFAULT_JNDI_SEARCH_ROOT);
    }

    private static int getValidationIntervalMillisFromEnv()
    {
        return Integer.parseInt(
                Optional.ofNullable(System.getenv(CASUAL_CALLER_VALIDATION_INTERVAL_ENV_NAME))
                        .orElse(DEFAULT_VALIDATION_INTERVAL_MILLIS));
    }

    private static boolean isTransactionStickyEnabledFromEnv()
    {
        return Boolean.parseBoolean(
                Optional.ofNullable(System.getenv(CASUAL_CALLER_TRANSACTION_STICKY_ENV_NAME))
                        .orElse(DEFAULT_TRANSACTION_STICKY));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuration that = (Configuration) o;
        return Objects.equals(this.getJndiSearchRoot(), that.getJndiSearchRoot())
                && this.getValidationIntervalMillis() == that.getValidationIntervalMillis()
                && this.isTransactionStickyEnabled() == that.isTransactionStickyEnabled();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(jndiSearchRoot, validationIntervalMillis, transactionStickyEnabled);
    }

    @Override
    public String toString()
    {
        return "Configuration{" +
                "jndiSearchRoot='" + jndiSearchRoot + '\'' +
                ", validationIntervalMillis=" + validationIntervalMillis +
                ", transactionStickyEnabled=" + transactionStickyEnabled +
                '}';
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String jndiSearchRoot;
        private Integer validationIntervalMillis;
        private Boolean transactionStickyEnabled;

        public Configuration build()
        {
            return new Configuration(this);
        }

        public Builder jndiSearchRoot(String jndiSearchRoot)
        {
            this.jndiSearchRoot = jndiSearchRoot;
            return this;
        }

        public Builder validationIntervalMillis(Integer validationIntervalMillis)
        {
            this.validationIntervalMillis = validationIntervalMillis;
            return this;
        }

        public Builder transactionStickyEnabled(Boolean transactionStickyEnabled)
        {
            this.transactionStickyEnabled = transactionStickyEnabled;
            return this;
        }
    }
}
