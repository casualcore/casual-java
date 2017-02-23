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
package se.kodarkatten.casual.jca;

import java.util.logging.Logger;

/**
 * CasualConnectionImpl
 *
 * @version $Revision: $
 */
public class CasualConnectionImpl implements CasualConnection
{
   /** The logger */
   private static Logger log = Logger.getLogger(CasualConnectionImpl.class.getName());

   /** ManagedConnection */
   private CasualManagedConnection mc;

   /** ManagedConnectionFactory */
   private CasualManagedConnectionFactory mcf;

   /**
    * Default constructor
    * @param mc CasualManagedConnection
    * @param mcf CasualManagedConnectionFactory
    */
   public CasualConnectionImpl(CasualManagedConnection mc, CasualManagedConnectionFactory mcf)
   {
      this.mc = mc;
      this.mcf = mcf;
   }

   /**
    * Call me
    */
   public void callMe()
   {
      if (mc != null)
         mc.callMe();
   }

   /**
    * Close
    */
   public void close()
   {
      if (mc != null)
      {
         mc.closeHandle(this);
         mc = null;
      }

   }

   /**
    * Set ManagedConnection
    */
   void setManagedConnection(CasualManagedConnection mc)
   {
      this.mc = mc;
   }

}
