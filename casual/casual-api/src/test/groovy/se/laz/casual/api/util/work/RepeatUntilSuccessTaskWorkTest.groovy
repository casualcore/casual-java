/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util.work

import spock.lang.Specification

import jakarta.resource.spi.work.WorkException
import jakarta.resource.spi.work.WorkManager
import java.util.function.Consumer
import java.util.function.Supplier

class RepeatUntilSuccessTaskWorkTest extends Specification
{
   def 'will retry once and then fail with CasualWorkException'()
   {
      given:
      def payload = 'hello'
      Supplier<String> supplier = {payload}
      Consumer<String> consumer = {it ->
         assert it == payload
      }
      WorkManager workManager = Mock(WorkManager){
         2 * scheduleWork( _ as RepeatUntilSuccessTaskWork, WorkManager.INDEFINITE, null, _ as RepeatUntilSuccessTaskWorkListener) >> {
            throw new WorkException("Due to some internal reason")}
      }
      Supplier<WorkManager> workManagerSupplier = {
         workManager
      }
      when:
      RepeatUntilSuccessTaskWork taskWork = RepeatUntilSuccessTaskWork.of(supplier, consumer, workManagerSupplier)
      taskWork.start()
      then:
      thrown(CasualWorkException)
   }

   def 'No work exception, only scheduled once'()
   {
      given:
      def payload = 'hello'
      Supplier<String> supplier = {payload}
      Consumer<String> consumer = {it ->
         assert it == payload
      }
      WorkManager workManager = Mock(WorkManager) {
         1 * scheduleWork(_ as RepeatUntilSuccessTaskWork, WorkManager.INDEFINITE, null, _ as RepeatUntilSuccessTaskWorkListener)
      }
      Supplier<WorkManager> workManagerSupplier = {
         workManager
      }
      when:
      RepeatUntilSuccessTaskWork taskWork = RepeatUntilSuccessTaskWork.of(supplier, consumer, workManagerSupplier)
      taskWork.start()
      then:
      noExceptionThrown()
   }
}
