/*
 * Copyright (c) 2021, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca.conversation

import spock.lang.Specification

class ConversationDirectionTest extends Specification
{
   def 'switch back and forth'()
   {
      when:
      ConversationDirection conversationDirection = ConversationDirection.RECEIVE
      then:
      conversationDirection.isReceive()
      when:
      conversationDirection = conversationDirection.switchDirection()
      then:
      conversationDirection.isSend()
      when:
      conversationDirection = conversationDirection.switchDirection()
      then:
      conversationDirection.isReceive()
   }
}
