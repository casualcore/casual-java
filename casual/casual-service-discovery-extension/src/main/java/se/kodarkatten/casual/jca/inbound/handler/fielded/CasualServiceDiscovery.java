package se.kodarkatten.casual.jca.inbound.handler.fielded;

import se.kodarkatten.casual.api.services.CasualService;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import java.lang.reflect.Method;
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

    @SuppressWarnings("unchecked")
    public void processAnnotatedType(@Observes @WithAnnotations({CasualService.class}) ProcessAnnotatedType<?> processAnnotatedType)
    {
        LOG.info("processAnnotatedType() start.");
        for( AnnotatedMethod<?> method: processAnnotatedType.getAnnotatedType().getMethods() )
        {
            CasualService service = method.getAnnotation(CasualService.class);
            if( service != null )
            {
                Class<?> serviceClass = processAnnotatedType.getAnnotatedType().getJavaClass();
                Method serviceMethod = method.getJavaMember();
                serviceRegistry.register(service, serviceMethod, serviceClass);
            }
        }
        LOG.info(()->"processAnnotatedType() end.");

    }

    public void afterBeanDiscovery(@Observes AfterBeanDiscovery abd)
    {
        LOG.info(()->"Services found: " + serviceRegistry.size() );
        LOG.info(()->"Service Discovery Done");
    }

}