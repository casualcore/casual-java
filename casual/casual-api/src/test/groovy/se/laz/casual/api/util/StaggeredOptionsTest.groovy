package se.laz.casual.api.util


import spock.lang.Specification

import java.time.Duration

class StaggeredOptionsTest extends Specification
{
   def 'staggerfactor of 0 or less than 0'()
   {
      given:
      Duration initialDelay = Duration.ofMillis(100L)
      Duration subsequentDelay = Duration.ofMillis(500L)
      Duration maxDelay = Duration.ofMillis(1000L)
      int staggerFactor = 0
      when:
      StaggeredOptions.createBuilder()
              .withInitialDelay(initialDelay)
              .withSubsequentDelay(subsequentDelay)
              .withMaxDelay(maxDelay)
              .withStaggerFactor(staggerFactor)
              .build()
      then:
      thrown(IllegalArgumentException)
      when:
      staggerFactor = -1
      StaggeredOptions.createBuilder()
              .withInitialDelay(initialDelay)
              .withSubsequentDelay(subsequentDelay)
              .withMaxDelay(maxDelay)
              .withStaggerFactor(staggerFactor)
              .build()
      then:
      thrown(IllegalArgumentException)
   }
   def 'staggerfactor of 1'()
   {
      given:
      Duration initialDelay = Duration.ofMillis(100L)
      Duration subsequentDelay = Duration.ofMillis(500L)
      Duration maxDelay = Duration.ofMillis(1000L)
      int staggerFactor = 1
      StaggeredOptions staggeredOptions = StaggeredOptions.createBuilder()
              .withInitialDelay(initialDelay)
              .withSubsequentDelay(subsequentDelay)
              .withMaxDelay(maxDelay)
              .withStaggerFactor(staggerFactor)
              .build()
      when: // initial
      Duration current = staggeredOptions.getNext()
      then:
      current.toMillis() == initialDelay.toMillis()
      when: // 1st subsequent
      current = staggeredOptions.getNext()
      then:
      current.toMillis() == initialDelay.toMillis() + subsequentDelay.toMillis()
      when: // maxDelay
      current = staggeredOptions.getNext()
      then:
      current.toMillis() == maxDelay.toMillis()
   }

   def 'staggerfactor of 2'()
   {
      given:
      Duration initialDelay = Duration.ofMillis(100L)
      Duration subsequentDelay = Duration.ofMillis(500L)
      Duration maxDelay = Duration.ofMillis(1000L)
      int staggerFactor = 2
      StaggeredOptions staggeredOptions = StaggeredOptions.createBuilder()
              .withInitialDelay(initialDelay)
              .withSubsequentDelay(subsequentDelay)
              .withMaxDelay(maxDelay)
              .withStaggerFactor(staggerFactor)
              .build()
      when: // initial
      Duration current = staggeredOptions.getNext()
      then:
      current.toMillis() == initialDelay.toMillis()
      when: // 1st subsequent
      current = staggeredOptions.getNext()
      then:
      current.toMillis() == maxDelay.toMillis()
   }

}
