package se.laz.casual.jca.cli;

import jakarta.enterprise.context.ApplicationScoped;
import se.laz.casual.config.ConfigurationOption;
import se.laz.casual.config.ConfigurationService;
import se.laz.casual.connection.jca.cli.model.Domain;
import se.laz.casual.connection.jca.cli.model.JCAConfiguration;
import se.laz.casual.jca.DomainId;

import java.util.Map;

import static se.laz.casual.config.ConfigurationOptions.CASUAL_DOMAIN_ID;
import static se.laz.casual.config.ConfigurationOptions.CASUAL_DOMAIN_NAME;

@ApplicationScoped
public class CLIService
{
    public JCAConfiguration getConfiguration()
    {
        Map<ConfigurationOption<?>, Object> configuration = ConfigurationService.getConfiguration();
        JCAConfiguration jcaConfiguration = new JCAConfiguration();
        Domain domain = new Domain();
        domain.setId(((DomainId) configuration.get(CASUAL_DOMAIN_ID)).getId().toString());
        domain.setName(configuration.get(CASUAL_DOMAIN_NAME).toString());
        jcaConfiguration.setDomain(domain);
        return jcaConfiguration;
    }
}
