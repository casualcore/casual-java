/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.inbound.handler.service.casual;

import se.laz.casual.api.service.CasualService;
import se.laz.casual.api.service.CasualServiceJndiName;

import jakarta.ejb.Remote;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import jakarta.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;

/**
 * CDI extension for discovering services to export to
 * casual
 */
public class CasualServiceDiscovery implements Extension
{
    private static final System.Logger LOG = System.getLogger(CasualServiceDiscovery.class.getName());

    private static final CasualServiceRegistry serviceRegistry = CasualServiceRegistry.getInstance();

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        LOG.log(System.Logger.Level.INFO,()->"Initializing service Discovery");
    }

    public <T> void processAnnotatedType(@Observes @WithAnnotations({CasualService.class}) ProcessAnnotatedType<T> processAnnotatedType )
    {
        LOG.log(System.Logger.Level.INFO,"processAnnotatedType() start.");

        AnnotatedType<T> type = processAnnotatedType.getAnnotatedType();

        CasualServiceJndiName casualServiceJndiName = type.getAnnotation( CasualServiceJndiName.class );
        Named named = type.getAnnotation( Named.class );
        String ejbName = (named!=null)? named.value(): null;

        Remote remote = type.getAnnotation( Remote.class );
        Class<?> remoteInterfaceClass = ( remote.value().length > 0 )? remote.value()[0] : null;

        Class<?> serviceClass = type.getJavaClass();

        String appname = null;
        String moduleName = null;
        try
        {
            appname = InitialContext.doLookup( "java:app/AppName" );
        }
        catch( NamingException e )
        {
            LOG.log(System.Logger.Level.TRACE, ()-> "Error retrieving app name." );
        }

        try
        {
            moduleName = InitialContext.doLookup( "java:module/ModuleName" );
        }
        catch( NamingException e )
        {
            LOG.log(System.Logger.Level.TRACE, ()-> "Error retrieving module name." );
        }

        CasualServiceMetaData.CasualServiceMetaDataBuilder b = CasualServiceMetaData.newBuilder()
                .serviceJndiName( casualServiceJndiName )
                .implementationClass( serviceClass )
                .interfaceClass( remoteInterfaceClass )
                .appName(appname)
                .moduleName(moduleName)
                .ejbName( ejbName );

        for( AnnotatedMethod<?> method: processAnnotatedType.getAnnotatedType().getMethods() )
        {
            CasualService service = method.getAnnotation(CasualService.class);
            if( service != null )
            {
                Method serviceMethod = method.getJavaMember();
                b.service( service ).serviceMethod( serviceMethod );
                serviceRegistry.register(b.build());
            }
        }
        LOG.log(System.Logger.Level.INFO,()->"processAnnotatedType() end.");
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd)
    {
        LOG.log(System.Logger.Level.INFO,()->"Services found: " + serviceRegistry.serviceMetaDataSize() );
        LOG.log(System.Logger.Level.INFO,()->"Service Discovery Done");
    }

}