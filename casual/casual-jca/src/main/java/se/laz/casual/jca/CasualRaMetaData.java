/*
 * Copyright (c) 2017 - 2018, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import se.laz.casual.internal.CasualConstants;

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
