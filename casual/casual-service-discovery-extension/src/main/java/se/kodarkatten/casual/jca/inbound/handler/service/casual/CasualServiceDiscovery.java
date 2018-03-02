package se.kodarkatten.casual.jca.inbound.handler.service.casual;

import se.kodarkatten.casual.api.service.CasualService;
import se.kodarkatten.casual.api.service.CasualServiceJndiName;

import javax.ejb.Remote;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.inject.Named;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CDI extension for discovering services to export to
 * casual
 */
public class CasualServiceDiscovery implements Extension
{
    private static final Logger LOG = Logger.getLogger(CasualServiceDiscovery.class.getName());

    private static final CasualServiceRegistry serviceRegistry = CasualServiceRegistry.getInstance();

    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        LOG.info(()->"Initializing service Discovery");
    }

    public <T> void processAnnotatedType(@Observes @WithAnnotations({CasualService.class}) ProcessAnnotatedType<T> processAnnotatedType )
    {
        LOG.info("processAnnotatedType() start.");

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
            LOG.log(Level.WARNING, e, ()-> "Error retrieving app name." );
        }

        try
        {
            moduleName = InitialContext.doLookup( "java:module/ModuleName" );
        }
        catch( NamingException e )
        {
            LOG.log(Level.WARNING, e, ()-> "Error retrieving module name." );
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
        LOG.info(()->"processAnnotatedType() end.");
    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd)
    {
        LOG.info(()->"Services found: " + serviceRegistry.serviceMetaDataSize() );
        LOG.info(()->"Service Discovery Done");
    }

}