package se.laz.casual.connection.caller.pool;

import se.laz.casual.connection.caller.ConnectionFactoryEntryStore;
import se.laz.casual.jca.CasualConnection;
import se.laz.casual.jca.DomainId;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.resource.ResourceException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PoolPoller
{
    private static final Logger LOG = Logger.getLogger(PoolPoller.class.getName());
    private static final long DEFAULT_TIME_MILLIS = 10 * 1000;
    public static final String ENV_VAR_NAME = "CASUAL_CALLER_POOL_UPDATE_INTERVAL";
    @Resource
    private TimerService timerService;
    @Inject
    private PoolManager poolManager;
    @Inject
    private ConnectionFactoryEntryStore connectionFactoryStore;

    @PostConstruct
    private void init()
    {
        long interval = DEFAULT_TIME_MILLIS;
        // Try read user config for timeout from env
        final String rawIntervalValue = System.getenv(ENV_VAR_NAME);
        if (rawIntervalValue != null && !rawIntervalValue.isEmpty())
        {
            try
            {
                interval = Long.parseLong(rawIntervalValue);
            }
            catch (NumberFormatException e)
            {
                LOG.severe("Invalid config value for casual caller pool poller timer.\n" + e);
            }
        }
        TimerConfig config = new TimerConfig();
        config.setPersistent(false);
        timerService.createIntervalTimer(0, interval, config);
    }

    @Timeout
    public void updatePools()
    {
        try
        {
            connectionFactoryStore.get().stream()
                                  .forEach( connectionFactoryEntry -> {
                                      try(CasualConnection connection = connectionFactoryEntry.getConnectionFactory().getConnection())
                                      {
                                          List<DomainId> domainIds = connection.getPoolDomainIds();
                                          poolManager.updatePool(connectionFactoryEntry, domainIds);
                                      }
                                      catch(ResourceException resourceException)
                                      {
                                          // this means we can not get any connection, it is gone
                                          poolManager.updatePool(connectionFactoryEntry, Collections.emptyList());
                                      }
                                      catch(Exception e)
                                      {
                                          LOG.warning(() -> "Failed updating: " + connectionFactoryEntry + " -> " + e);
                                      }
                                  });
        }
        catch(Exception e)
        {
            LOG.warning(() -> "failed polling pools, reason: " + e);
        }
    }

}
