package se.laz.casual.jca.cli;

import jakarta.enterprise.context.ApplicationScoped;
import se.laz.casual.config.ConfigurationOption;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.jca.DomainId;
import se.laz.casual.jca.cli.model.Configuration;

import java.util.Map;

import static se.laz.casual.config.ConfigurationOptions.CASUAL_DOMAIN_ID;
import static se.laz.casual.config.ConfigurationOptions.CASUAL_DOMAIN_NAME;

@ApplicationScoped
public class CLIService
{
    public Configuration getConfiguration()
    {
        Map<ConfigurationOption<?>, Object> configuration = ConfigurationService.getConfiguration();
        return new Configuration(new se.laz.casual.jca.cli.model.Domain(
                ((DomainId) configuration.get(CASUAL_DOMAIN_ID)).getId().toString(),
                configuration.get(CASUAL_DOMAIN_NAME).toString()
        ));
    }
}
