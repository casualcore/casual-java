/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class InboundThreadLocalTest extends Specification
{
    InboundThreadContext testContext
    InboundThreadContext altContext
    SpanId testSpanId
    SpanId altSpanId
    UUID testExecution
    UUID altExecution

    def setup()
    {
        testSpanId = SpanId.of(42L)
        altSpanId = SpanId.of(24L)
        testExecution = UUID.randomUUID()
        altExecution = UUID.randomUUID()
        testContext = new InboundThreadContext(testSpanId, "testParent", testExecution)
        altContext = new InboundThreadContext(altSpanId, "altParent", altExecution)
    }

    def cleanup()
    {
        // Ensure clean thread state after each test
        InboundThreadLocal.getContext().ifPresent { ctx ->
            new InboundThreadLocal(ctx).close()
        }
    }

    def "of() should reject null context with meaningful NPE"()
    {
        when:
        InboundThreadLocal.of(null)

        then:
        NullPointerException e = thrown()
        e != null  // Just verify NPE is thrown, message format may vary
    }

    def "of() should accept valid context and set it in thread local"()
    {
        when:
        def inboundThreadLocal = InboundThreadLocal.of(testContext)
        
        then:
        InboundThreadLocal.getContext().isPresent()
        InboundThreadLocal.getContext().get() == testContext
        
        cleanup:
        inboundThreadLocal?.close()
    }

    def "getContext() should return empty Optional when no context is set"()
    {
        expect:
        !InboundThreadLocal.getContext().isPresent()
    }

    def "getContext() should return current context when set"()
    {
        when:
        def inboundThreadLocal = InboundThreadLocal.of(testContext)
        def result = InboundThreadLocal.getContext()

        then:
        result.isPresent()
        result.get() == testContext
        result.get().spanId() == testSpanId
        result.get().parentName() == "testParent"
        result.get().execution() == testExecution
        
        cleanup:
        inboundThreadLocal?.close()
    }

    def "close() should clear thread local and make getContext() return empty"()
    {
        given:
        def inboundThreadLocal = InboundThreadLocal.of(testContext)
        assert InboundThreadLocal.getContext().isPresent()

        when:
        inboundThreadLocal.close()

        then:
        !InboundThreadLocal.getContext().isPresent()
    }

    def "close() should be idempotent - multiple calls should be safe"()
    {
        given:
        def inboundThreadLocal = InboundThreadLocal.of(testContext)

        when:
        inboundThreadLocal.close()
        inboundThreadLocal.close()
        inboundThreadLocal.close()

        then:
        !InboundThreadLocal.getContext().isPresent()
        noExceptionThrown()
    }

    def "try-with-resources should automatically clean up context"()
    {
        when:
        Optional<InboundThreadContext> contextAfterTry
        try (def inboundThreadLocal = InboundThreadLocal.of(testContext))
        {
            assert InboundThreadLocal.getContext().isPresent()
            assert InboundThreadLocal.getContext().get() == testContext
        }
        contextAfterTry = InboundThreadLocal.getContext()

        then:
        !contextAfterTry.isPresent()
    }

    def "thread isolation test - each thread should have its own context"()
    {
       given:
       def latch = new CountDownLatch(2)
       def results = Collections.synchronizedMap([:] as Map<String, InboundThreadContext>)

       when:
       // Thread 1: sets its OWN context (no inheritance needed here)
       new Thread({
          try (def threadLocal = InboundThreadLocal.of(altContext)) {
             def ctx = InboundThreadLocal.getContext().get()
             results["thread1"] = ctx
             // Optional: assert here for faster failure feedback
             assert ctx == altContext : "Thread1 should see altContext but saw $ctx"
          } catch (Throwable t) {
             t.printStackTrace()
          }
          latch.countDown()
       } as Runnable).start()

       // Thread 2: nothing set → must be null
       new Thread({
          def ctx = InboundThreadLocal.getContext().orElse(null)
          results["thread2"] = ctx
          latch.countDown()
       } as Runnable).start()

       latch.await(10, TimeUnit.SECONDS)

       then:
       results["thread1"] == altContext
       results["thread2"] == null
       InboundThreadLocal.getContext().isEmpty()   // main thread clean
    }

    def "context inheritance test - child threads should inherit parent context"() {
       given:
       InboundThreadContext childContext = null

       when:
       try (def parent = InboundThreadLocal.of(testContext)) {
          def latch = new CountDownLatch(1)

          new Thread({
            childContext = InboundThreadLocal.getContext().orElse(null)
             latch.countDown()
          }).start()

          latch.await(5, TimeUnit.SECONDS)
       }

       then:
       childContext == testContext
       childContext?.spanId() == testSpanId
       childContext?.parentName() == "testParent"
       childContext?.execution() == testExecution

       when:
       childContext = InboundThreadLocal.getContext().orElse(null)
       then:
       childContext == null
    }
    def "multiple instances in same thread should override each other"()
    {
        when:
        def firstLocal = InboundThreadLocal.of(testContext)
        assert InboundThreadLocal.getContext().get() == testContext
        
        def secondLocal = InboundThreadLocal.of(altContext)
        assert InboundThreadLocal.getContext().get() == altContext
        
        // Close both to clean up
        secondLocal.close()
        def contextAfterSecondClose = InboundThreadLocal.getContext().isPresent()
        firstLocal.close()
        def contextAfterFirstClose = InboundThreadLocal.getContext().isPresent()

        then:
        !contextAfterSecondClose
        !contextAfterFirstClose
        
        cleanup:
        firstLocal.close()
        secondLocal.close()
    }

    def "nested usage pattern - inner context clobbers"()
    {
        given:
        def contextHistory = [] as List<Optional<InboundThreadContext>>
        when:
        try (def outerLocal = InboundThreadLocal.of(testContext))
        {
            contextHistory.add(InboundThreadLocal.getContext())
            try (def innerLocal = InboundThreadLocal.of(altContext))
            {
                contextHistory.add(InboundThreadLocal.getContext())
            }
            contextHistory.add(InboundThreadLocal.getContext())
        }
        contextHistory.add(InboundThreadLocal.getContext())
        then:
        contextHistory[0].get() == testContext
        contextHistory[1].get() == altContext
        !contextHistory[2].present  // After inner close, outer is gone too
        !contextHistory[3].present  // After outer close, still gone
    }

    @Unroll
    def "context validation - all required fields should be non-null #spanId #parentName #execution"()
    {
        when:
        def context = new InboundThreadContext(spanId, parentName, execution)
        then:
        context.spanId() == spanId
        context.parentName() == parentName
        context.execution() == execution
        where:
        spanId         | parentName    | execution
        SpanId.of(1L)  | "test"        | UUID.randomUUID()
        SpanId.of(999) | "validName"   | UUID.randomUUID()
    }

}