package se.laz.casual.network.inbound.reverse

import spock.lang.Specification

import java.time.Duration

class StaggeredOptionsTest extends Specification
{
   def 'test'()
   {
      given:
      // StaggeredOptions(Duration initialDelay, Duration subsequentDelay, Duration maxDelay, int staggerFactor)
      Duration initialDelay = Duration.ofMillis(100L)
      Duration subsequentDelay = Duration.ofMillis(250L)
      Duration maxDelay = Duration.ofSeconds(2)
      int staggerFactor = 1
      StaggeredOptions staggeredOptions = StaggeredOptions.of(initialDelay, subsequentDelay, maxDelay, staggerFactor)
      when:
      Duration current = staggeredOptions.getNext()
      then: // initial
      current.toMillis() == initialDelay.toMillis()
      when:
      current = staggeredOptions.getNext()
      then: // 1st subsequent
      current.toMillis() == initialDelay.toMillis() + subsequentDelay.toMillis()
   }
}
