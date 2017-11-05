/*
 * IronJacamar, a Java EE Connector Architecture implementation
 * Copyright 2013, Red Hat Inc, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package se.kodarkatten.casual.jca.inflow;

import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import java.util.logging.Logger;

/**
 * CasualActivationSpec
 *
 * @version $Revision: $
 */
@Activation(messageListeners = { CasualMessageListener.class })
public class CasualActivationSpec implements ActivationSpec
{
   private static Logger logger = Logger.getLogger(CasualActivationSpec.class.getName());
   private ResourceAdapter ra;

   private int port = 7772;

   public int getPort()
   {
      return port;
   }

   public void setPort( int port )
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
