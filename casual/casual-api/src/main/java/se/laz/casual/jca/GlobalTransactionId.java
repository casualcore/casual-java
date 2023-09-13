/*
 * Copyright (c) 2023, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.jca;

import java.util.Arrays;
import java.util.Objects;

public class GlobalTransactionId
{
   private final byte[] data;

   private GlobalTransactionId(byte[] data)
   {
      this.data = data;
   }

   public static GlobalTransactionId of(byte[] data)
   {
      Objects.requireNonNull(data);
      return new GlobalTransactionId(data);
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      GlobalTransactionId that = (GlobalTransactionId) o;
      return Arrays.equals(data, that.data);
   }

   @Override
   public int hashCode()
   {
      return Arrays.hashCode(data);
   }
}
