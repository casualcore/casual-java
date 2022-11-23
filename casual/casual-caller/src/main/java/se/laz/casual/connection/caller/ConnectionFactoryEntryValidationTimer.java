/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.connection.caller;

import se.laz.casual.connection.caller.config.ConfigurationService;

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
import java.util.logging.Logger;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ConnectionFactoryEntryValidationTimer
{
    private static final Logger LOG = Logger.getLogger(ConnectionFactoryEntryValidationTimer.class.getName());

    @Resource
    private TimerService timerService;

    @Inject
    private ConnectionFactoryEntryStore connectionFactoryStore;

    @Inject
    ConnectionValidator connectionValidator;

    @PostConstruct
    private void setup()
    {
        long interval = ConfigurationService.getInstance().getConfiguration().getValidationIntervalMillis();

        // Setup timer
        TimerConfig config = new TimerConfig();
        config.setPersistent(false);
        timerService.createIntervalTimer(0, interval, config);
    }

    @Timeout
    public void validateConnectionFactories()
    {
        LOG.finest("Running ConnectionFactoryEntryValidationTimer");
        try
        {
            connectionFactoryStore.get()
                                  .forEach( connectionFactoryEntry -> {
                                             try
                                             {
                                                 connectionValidator.validate(connectionFactoryEntry);
                                             }
                                             catch(Exception e)
                                             {
                                                 connectionFactoryEntry.invalidate();
                                                 LOG.warning(() -> "Failed validating: " + connectionFactoryEntry + " -> " + e);
                                             }
                                     });
        }
        catch(Exception e)
        {
            LOG.warning(() -> "failed validating connection factories: " + e);
        }
    }
}
