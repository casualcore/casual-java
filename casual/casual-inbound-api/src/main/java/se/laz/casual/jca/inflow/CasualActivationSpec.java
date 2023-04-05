/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca.inflow;

import jakarta.resource.spi.Activation;
import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.InvalidPropertyException;
import jakarta.resource.spi.ResourceAdapter;
import java.util.logging.Logger;

/**
 * Activation Specification for Inbound Message Listener.
 */
@Activation(messageListeners = { CasualMessageListener.class })
public class CasualActivationSpec implements ActivationSpec
{
   private static Logger logger = Logger.getLogger(CasualActivationSpec.class.getName());
   private ResourceAdapter ra;

   private Integer port;

   public Integer getPort()
   {
      return port;
   }

   public void setPort( Integer port )
   {
      this.port = port;
   }

   @Override
   public void validate() throws InvalidPropertyException
   {
      logger.finest("validate()");

   }

   @Override
   public ResourceAdapter getResourceAdapter()
   {
      logger.finest("getResourceAdapter()");
      return ra;
   }

   @Override
   public void setResourceAdapter(ResourceAdapter ra)
   {
      logger.finest("setResourceAdapter()");
      this.ra = ra;
   }


}
