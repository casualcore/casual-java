/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.util;

import java.time.Duration;
import java.util.Objects;
import java.util.logging.Logger;

public class StaggeredOptions
{
    private static final Logger LOG = Logger.getLogger(StaggeredOptions.class.getName());
    private final Duration initialDelay;
    private final Duration subsequentDelay;
    private final Duration maxDelay;
    private Duration currentDelay;
    private int staggerFactor;
    private boolean initial = true;

    private StaggeredOptions(Duration initialDelay, Duration subsequentDelay, Duration maxDelay, int staggerFactor)
    {
        this.initialDelay = initialDelay;
        this.subsequentDelay = subsequentDelay;
        this.maxDelay = maxDelay;
        this.staggerFactor = staggerFactor;
    }

   private static StaggeredOptions of(Duration initialDelay, Duration subsequentDelay, Duration maxDelay, int staggerFactor)
   {
      Objects.requireNonNull(initialDelay, "initialDelay can not be null");
      Objects.requireNonNull(subsequentDelay, "subsequentDelay can not be null");
      Objects.requireNonNull(maxDelay, "maxDelay can not be null");
      if(staggerFactor <= 0)
      {
         throw new IllegalArgumentException("staggerFactor equal to or below zero is not supported");
      }
      return new StaggeredOptions(initialDelay, subsequentDelay, maxDelay, staggerFactor);
   }

   public Duration getNext()
   {
      if(initial)
      {
         initial = false;
         currentDelay = initialDelay;
         return currentDelay;
      }
      currentDelay = currentDelay.plus(Duration.ofMillis(subsequentDelay.toMillis() * staggerFactor));
      currentDelay  = currentDelay.compareTo(maxDelay) > 0 ? maxDelay : currentDelay;
      LOG.finest(() -> " delay: " + currentDelay);
      return currentDelay;
   }

   public static Builder createBuilder()
   {
      return new Builder();
   }

   public static final class Builder
   {
      private Duration initialDelay;
      private Duration subsequentDelay;
      private Duration maxDelay;
      private int staggerFactor;

      public Builder withInitialDelay(Duration initialDelay)
      {
         this.initialDelay = initialDelay;
         return this;
      }

      public Builder withSubsequentDelay(Duration subsequentDelay)
      {
         this.subsequentDelay = subsequentDelay;
         return this;
      }

      public Builder withMaxDelay(Duration maxDelay)
      {
         this.maxDelay = maxDelay;
         return this;
      }

      public Builder withStaggerFactor(int staggerFactor)
      {
         this.staggerFactor = staggerFactor;
         return this;
      }

      public StaggeredOptions build()
      {
         return StaggeredOptions.of(initialDelay, subsequentDelay, maxDelay, staggerFactor);
      }
   }
}
