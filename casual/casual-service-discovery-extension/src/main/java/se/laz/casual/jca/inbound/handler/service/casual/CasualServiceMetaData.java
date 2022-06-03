/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.api.service.CasualServiceJndiName;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

/**
 * Class to store all meta data about a declared service that is discovered
 * during the extension observation from {@link CasualServiceDiscovery}.
 *
 * This information will be used in order to resolve the service a runtime
 * and determine the correct endpoint information that should be used
 * during dispatch.
 */
public final class CasualServiceMetaData
{
    private final CasualService service;
    private final CasualServiceJndiName jndiName;
    private final Class<?> implementationClass;
    private final Class<?> interfaceClass;
    private final Method serviceMethod;
    private final String appName;
    private final String moduleName;
    private final String ejbName;
    private CasualServiceEntry resolvedEntry;

    private CasualServiceMetaData( CasualServiceMetaDataBuilder builder )
    {
        Objects.requireNonNull( builder.service, "Casual Service must be provided." );
        Objects.requireNonNull( builder.implementationClass, "Implementation class must be provided." );
        Objects.requireNonNull( builder.serviceMethod, "Service method must be provided." );

        this.service = builder.service;
        this.jndiName = builder.jndiName;
        this.implementationClass = builder.implementationClass;
        this.interfaceClass = builder.interfaceClass;
        this.serviceMethod = builder.serviceMethod;
        this.appName = builder.appName;
        this.moduleName = builder.moduleName;
        this.ejbName = builder.ejbName;
    }

    public static CasualServiceMetaDataBuilder newBuilder()
    {
        return new CasualServiceMetaDataBuilder();
    }

    public String getServiceName()
    {
        return this.service.name();
    }

    public String getServiceCategory()
    {
        return this.service.category();
    }

    public Optional<String> getJndiName()
    {
        return jndiName == null ? Optional.empty() : Optional.of( jndiName.value() );
    }

    public Class<?> getImplementationClass()
    {
        return this.implementationClass;
    }

    public Optional<Class<?>> getInterfaceClass()
    {
        return Optional.ofNullable( this.interfaceClass );
    }

    public Method getServiceMethod()
    {
        return this.serviceMethod;
    }

    public Optional<String> getAppName()
    {
        return Optional.ofNullable( this.appName );
    }

    public Optional<String> getModuleName()
    {
        return Optional.ofNullable( this.moduleName );
    }

    public Optional<String> getEjbName()
    {
        return Optional.ofNullable( this.ejbName );
    }

    public void setResolvedEntry( CasualServiceEntry entry )
    {
        this.resolvedEntry = entry;
    }

    public Optional<CasualServiceEntry> getResolvedEntry()
    {
        return Optional.ofNullable( this.resolvedEntry );
    }

    public boolean isUnresolved()
    {
        return resolvedEntry == null;
    }

    @Override
    public String toString()
    {
        return "CasualServiceMetaData{" +
                "service=" + service +
                ", jndiName=" + jndiName +
                ", implementationClass=" + implementationClass +
                ", interfaceClass=" + interfaceClass +
                ", serviceMethod=" + serviceMethod +
                ", appName='" + appName + '\'' +
                ", moduleName='" + moduleName + '\'' +
                ", ejbName='" + ejbName + '\'' +
                ", resolvedEntry=" + resolvedEntry +
                '}';
    }

    public static class CasualServiceMetaDataBuilder
    {
        private CasualService service;
        private CasualServiceJndiName jndiName;
        private Class<?> implementationClass;
        private Class<?> interfaceClass;
        private Method serviceMethod;
        private String appName;
        private String moduleName;
        private String ejbName;

        /**
         * The annotation from the annotated type found during discovery.
         * @param c the annotation.
         * @return builder
         */
        public CasualServiceMetaDataBuilder service( CasualService c)
        {
            this.service = c;
            return this;
        }

        /**
         * The annotation from the annotated type found during discovery if present.
         * @param name the annotation.
         * @return builder
         */
        public CasualServiceMetaDataBuilder serviceJndiName( CasualServiceJndiName name )
        {
            this.jndiName = name;
            return this;
        }

        /**
         * The implementation class from the annotated type found during discovery.
         * @param implementationClass the implementation class.
         * @return builder
         */
        public CasualServiceMetaDataBuilder implementationClass( Class<?> implementationClass )
        {
            this.implementationClass = implementationClass;
            return this;
        }

        /**
         * The interface value of the {@link javax.ejb.Remote} annotation if present.
         * @param interfaceClass the interface class
         * @return builder
         */
        public CasualServiceMetaDataBuilder interfaceClass( Class<?> interfaceClass )
        {
            this.interfaceClass = interfaceClass;
            return this;
        }


        /**
         * The method with the {@link CasualService} annotation applied.
         * @param m annotated method.
         * @return builder
         */
        public CasualServiceMetaDataBuilder serviceMethod( Method m )
        {
            this.serviceMethod = m;
            return this;
        }

        /**
         * The current application name determined during discovery.
         * @param appName application  name.
         * @return builder
         */
        public CasualServiceMetaDataBuilder appName( String appName )
        {
            this.appName = appName;
            return this;
        }

        /**
         * The current module name determined during discovery.
         * @param moduleName module name
         * @return builder
         */
        public CasualServiceMetaDataBuilder moduleName( String moduleName )
        {
            this.moduleName = moduleName;
            return this;
        }

        /**
         * The declared ejb name if present during discovery.
         * @param ejbName ejb name.
         * @return builder
         */
        public CasualServiceMetaDataBuilder ejbName( String ejbName )
        {
            this.ejbName = ejbName;
            return this;
        }

        /**
         * Generate a {@link CasualServiceMetaData} instance from the supplied
         * arguments.
         * @return new meta data object.
         */
        public CasualServiceMetaData build()
        {
            return new CasualServiceMetaData( this );
        }
    }
}