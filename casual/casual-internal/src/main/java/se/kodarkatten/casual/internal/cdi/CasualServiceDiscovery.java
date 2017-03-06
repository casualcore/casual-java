package se.kodarkatten.casual.internal.cdi;

import se.kodarkatten.casual.api.services.CasualService;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * CDI extension for discovering services to export to
 * casual
 */
public class CasualServiceDiscovery implements Extension
{
    private static final Logger LOG = Logger.getLogger(CasualServiceDiscovery.class.getName());

    @Inject
    private CasualServiceRegistry serviceRegistry;

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery)
    {
        LOG.config("Initializing service Discovery");
    }

    /**
     * This function will be invoked for every annotated type found
     */
    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> processAnnotatedType)
    {
        processAnnotatedType.getAnnotatedType().getMethods().stream().forEach(annotatedMethod ->
        {
            CasualService service = annotatedMethod.getAnnotation(CasualService.class);
            Class<?> serviceClass = processAnnotatedType.getAnnotatedType().getJavaClass();
            Method serviceMethod = annotatedMethod.getJavaMember();
            serviceRegistry.register(service, serviceMethod, serviceClass);

        });
    }

    void afterBeanDiscovery(@Observes AfterBeanDiscovery abd)
    {
        LOG.config("Service Discovery Done");
    }
}