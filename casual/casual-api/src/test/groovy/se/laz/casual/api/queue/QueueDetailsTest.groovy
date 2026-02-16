/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.api.queue


import se.laz.casual.network.ProtocolVersion
import spock.lang.Shared
import spock.lang.Specification

class QueueDetailsTest extends Specification
{
   @Shared
   def qName = 'theQueue'
   def 'construction'() {
      when:
      def details = QueueDetails.createBuilder()
              .withName(qName)
              .withRetries(retryies)
              .withProtocolVersion(protocolVersion)
              .withRetryDelay(retryDelay)
              .withEnqueueEnabled(enqueueEnabled)
              .withDequeueEnabled(dequeEnabled)
              .build()
      then:
      details.getName() == name
      details.getRetries() == retryies
      if(ProtocolVersion.isProtocolVersionGreaterOrEqualToOneFour(protocolVersion))
      {
         !details.getRetryDelay().isEmpty()
         !details.isDequeueEnabled().isEmpty()
         !details.isEnqueueEnabled().isEmpty()
      }
      else
      {
         details.getRetryDelay().isEmpty()
         details.isDequeueEnabled().isEmpty()
         details.isEnqueueEnabled().isEmpty()
      }
      where:
      protocolVersion             | name  | retryies | retryDelay | enqueueEnabled | dequeEnabled
      ProtocolVersion.VERSION_1_0 | qName | 3        | null        | null           | null
      ProtocolVersion.VERSION_1_1 | qName | 3        | null        | null           | null
      ProtocolVersion.VERSION_1_2 | qName | 3        | null        | null           | null
      ProtocolVersion.VERSION_1_3 | qName | 3        | null        | null           | null
      ProtocolVersion.VERSION_1_4 | qName | 3        | 42L         | false          | true
   }

}
