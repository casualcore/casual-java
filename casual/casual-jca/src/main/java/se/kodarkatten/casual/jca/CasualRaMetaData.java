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

import se.kodarkatten.casual.internal.CasualConstants;

import javax.resource.cci.ResourceAdapterMetaData;

/**
 * CasualRaMetaData
 *
 * @version $Revision: $
 */
public class CasualRaMetaData implements ResourceAdapterMetaData
{

   @Override
   public String getAdapterVersion()
   {
      return CasualConstants.CASUAL_API_VERSION;
   }

   @Override
   public String getAdapterVendorName()
   {
      return CasualConstants.CASUAL_NAME;
   }

   @Override
   public String getAdapterName()
   {
      return CasualConstants.CASUAL_ADAPTER_NAME;
   }

   @Override
   public String getAdapterShortDescription()
   {
      return CasualConstants.CASUAL_ADAPTER_DESCRIPTION;
   }

   @Override
   public String getSpecVersion()
   {
      return CasualConstants.CASUAL_ADAPTER_JCA_SPEC_VERSION;
   }

   @Override
   public String[] getInteractionSpecsSupported()
   {
      return null; //TODO
   }

   @Override
   public boolean supportsExecuteWithInputAndOutputRecord()
   {
      return false; //TODO
   }

   @Override
   public boolean supportsExecuteWithInputRecordOnly()
   {
      return false; //TODO
   }

   @Override
   public boolean supportsLocalTransactionDemarcation()
   {
      return false; //TODO
   }


}
