/*
 * Copyright (c) 2022, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */
package se.laz.casual.api.util.work

import spock.lang.Specification

import jakarta.resource.spi.work.WorkException
import jakarta.resource.spi.work.WorkManager
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
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
      RepeatUntilSuccessTaskWork taskWork = RepeatUntilSuccessTaskWork.of(supplier, consumer, workManagerSupplier,
              1000, Mock(ScheduledExecutorService))
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
      RepeatUntilSuccessTaskWork taskWork = RepeatUntilSuccessTaskWork.of(supplier, consumer, workManagerSupplier,
              1000, Mock(ScheduledExecutorService))
      taskWork.start()
      then:
      noExceptionThrown()
   }

   def 'reschedules work after each failure and backoff keeps increasing for 10 failures'()
   {
      given:
      RepeatUntilSuccessTaskWork taskWork
      long maxBackoff = 1969

      long lastBackoff = -1 // backoff is never negative, just a smaller-than-possible backoff to start comparing to
      ScheduledExecutorService mockScheduler = Mock(ScheduledExecutorService) {
         // 10 failures will lead to 10 reschedules by the ScheduledExecutorService
         10 * schedule(_ as Runnable, _ as Long, TimeUnit.MILLISECONDS) >> { arguments ->
            Runnable runnable = arguments.get(0)
            long backoff = arguments.get(1)

            // Check backoff is increasing for each failure and reschedule
            assert backoff > lastBackoff // Please note, this assert can fail fore sufficiently small max backoffs
            assert backoff <= maxBackoff
            assert backoff >= maxBackoff.intdiv(10)

            lastBackoff = backoff

            runnable.run()
         }
      }

      Consumer<String> consumer = Mock(Consumer) {
         10 * accept(_) >> {
            throw new ConnectException("Connection failed, have fun")
         }
         1 * accept(_) >> {
            //Yay?
         }
      }

      WorkManager workManager = Mock(WorkManager) {
         // 10 failures + 1 last successful run
         11 * scheduleWork(_ as RepeatUntilSuccessTaskWork, WorkManager.INDEFINITE, null, _ as RepeatUntilSuccessTaskWorkListener) >> {
            taskWork.run()
         }
      }

      taskWork = RepeatUntilSuccessTaskWork.of(()->'some sought after result', consumer, () -> workManager, maxBackoff, mockScheduler)

      when:
      taskWork.start()

      then:
      taskWork.backoffHelper.maxBackoffMillis == maxBackoff
      taskWork.backoffHelper.failures == 10
      noExceptionThrown()
   }
}
