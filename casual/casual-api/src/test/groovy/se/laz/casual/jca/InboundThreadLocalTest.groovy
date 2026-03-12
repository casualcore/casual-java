/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.api.concurrency.Concurrent
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

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
       InboundThreadLocal.remove()
    }

    def "of() should reject null context with meaningful NPE"()
    {
        when:
        InboundThreadLocal.of(null)
        then:
        thrown NullPointerException
    }

    def "of() should accept valid context and set it in thread local"()
    {
        when:
        InboundThreadLocal.of(testContext)
        then:
        InboundThreadLocal.getContext().isPresent()
        InboundThreadLocal.getContext().get() == testContext
    }

    def "getContext() should return empty Optional when no context is set"()
    {
        expect:
        InboundThreadLocal.getContext().isEmpty()
    }

    def "getContext() should return current context when set"()
    {
        when:
        InboundThreadLocal.of(testContext)
        def result = InboundThreadLocal.getContext()

        then:
        result.isPresent()
        result.get() == testContext
    }

    def "close() should clear thread local and make getContext() return empty"()
    {
        given:
        def inboundThreadLocal = InboundThreadLocal.of(testContext)
        assert InboundThreadLocal.getContext().isPresent()

        when:
        inboundThreadLocal.close()

        then:
        InboundThreadLocal.getContext().isEmpty()
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
        InboundThreadLocal.getContext().isEmpty()
    }

    def "try-with-resources should automatically clean up context"()
    {
        when:
        Optional<InboundThreadContext> contextAfterTry
        try (def ignored = InboundThreadLocal.of(testContext))
        {
            assert InboundThreadLocal.getContext().isPresent()
            assert InboundThreadLocal.getContext().get() == testContext
        }
        contextAfterTry = InboundThreadLocal.getContext()

        then:
        contextAfterTry.isEmpty()
    }

    def "context inheritance test - child threads should inherit parent context"() {
       given:
       InboundThreadContext childContext = null

       when:
       try (def ignored = InboundThreadLocal.of(testContext)) {
          def latch = new CountDownLatch(1)

          def work = {
             childContext = InboundThreadLocal.getContext().orElse(null)
             latch.countDown()
          }
          def wrapped = Concurrent.wrap(work)

          new Thread(wrapped).start()

          latch.await(5, TimeUnit.SECONDS)
       }

       then:
       childContext == testContext

       when:
       childContext = InboundThreadLocal.getContext().orElse(null)
       then:
       childContext == null
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

    def 'does not propagate to pooled thread if not wrapped'()
    {
       given:
       def capturedInAsync = new AtomicReference<InboundThreadContext>()
       when:
       try (def ignored = InboundThreadLocal.of(testContext)) {
          def executor = Executors.newFixedThreadPool(2)

          executor.submit({
             capturedInAsync.set(InboundThreadLocal.getContext().orElse(null))
          } as Runnable)

          executor.shutdown()
          executor.awaitTermination(5, TimeUnit.SECONDS)
       }

       then:
       capturedInAsync.get() == null
       InboundThreadLocal.getContext().isEmpty()
    }

   def 'does propagate to pooled thread when wrapped'()
   {
      given:
      def capturedInAsync = new AtomicReference<InboundThreadContext>()
      when:
      try (def ignored = InboundThreadLocal.of(testContext))
      {
         def executor = Executors.newFixedThreadPool(2)
         Runnable work = {
            capturedInAsync.set(InboundThreadLocal.getContext().orElse(null))
         }
         executor.submit(Concurrent.wrap(work))
         executor.shutdown()
         executor.awaitTermination(5, TimeUnit.SECONDS)
      }
      then:
      capturedInAsync.get() == testContext
      InboundThreadLocal.getContext().isEmpty()
   }

   def 'does propagate to pooled thread when wrapped - using Callable'()
   {
      given:
      def capturedInAsync = new AtomicReference<InboundThreadContext>()
      when:
      def result
      try (def ignored = InboundThreadLocal.of(testContext))
      {
         def executor = Executors.newFixedThreadPool(2)
         Callable<InboundThreadContext> work = new Callable<InboundThreadContext>() {
            @Override
            InboundThreadContext call() throws Exception {
               def context = InboundThreadLocal.getContext().orElse(null)
               capturedInAsync.set(context)
               return context
            }
         }

         Callable<InboundThreadContext> wrappedCallable = Concurrent.wrap(work)
         def future = executor.submit(wrappedCallable)
         result = future.get(5, TimeUnit.SECONDS)
         executor.shutdown()
         executor.awaitTermination(5, TimeUnit.SECONDS)
      }
      then:
      capturedInAsync.get() == testContext
      result == testContext
      InboundThreadLocal.getContext().isEmpty()
   }

   def 'wrapped Callable does nothing when no context is active'()
   {
      given:
      def captured = new AtomicReference<InboundThreadContext>()

      when:
      def executor = Executors.newFixedThreadPool(1)
      Callable<InboundThreadContext> work = new Callable<InboundThreadContext>() {
         @Override
         InboundThreadContext call() throws Exception {
            def context = InboundThreadLocal.getContext().orElse(null)
            captured.set(context)
            return context
         }
      }

      Callable<InboundThreadContext> wrappedCallable = Concurrent.wrap(work)
      def future = executor.submit(wrappedCallable)
      def result = future.get(5, TimeUnit.SECONDS)
      executor.shutdown()
      executor.awaitTermination(5, TimeUnit.SECONDS)

      then:
      captured.get() == null
      result == null
   }

   def 'wrapped Supplier should propagate context to pooled thread (CompletableFuture style)'() {
      given:
      def capturedInAsync = new AtomicReference<InboundThreadContext>()

      when:
      def result
      try (def ignored = InboundThreadLocal.of(testContext)) {
         def executor = Executors.newFixedThreadPool(2)

         Supplier<InboundThreadContext> supplier = new Supplier<InboundThreadContext>() {
            @Override
            InboundThreadContext get() {
               def context = InboundThreadLocal.getContext().orElse(null)
               capturedInAsync.set(context)
               return context
            }
         }

         def wrappedSupplier = Concurrent.wrap(supplier)

         def future = CompletableFuture.supplyAsync(wrappedSupplier, executor)
         result = future.get(5, TimeUnit.SECONDS)

         executor.shutdown()
         executor.awaitTermination(5, TimeUnit.SECONDS)
      }

      then:
      capturedInAsync.get() == testContext
      result == testContext
      InboundThreadLocal.getContext().isEmpty()
   }

   def 'wrapped Supplier does nothing when no context is active'()
   {
      given:
      def captured = new AtomicReference<InboundThreadContext>()

      when:
      def executor = Executors.newFixedThreadPool(1)
      def wrapped = Concurrent.wrap({
         captured.set(InboundThreadLocal.getContext().orElse(null))
         return "done"
      } as Supplier<String>)

      CompletableFuture.supplyAsync(wrapped, executor).get(5, TimeUnit.SECONDS)

      then:
      captured.get() == null
   }

   def 'context survives multiple pooled tasks from same outer scope'()
   {
      given:
      def captured1 = new AtomicReference<InboundThreadContext>()
      def captured2 = new AtomicReference<InboundThreadContext>()
      def captured3 = new AtomicReference<InboundThreadContext>()

      when:
      try (def ignored = InboundThreadLocal.of(testContext))
      {
         def executor = Executors.newFixedThreadPool(3)
         executor.submit(Concurrent.wrap( { captured1.set(InboundThreadLocal.getContext().orElse(null)) }))
         executor.submit(Concurrent.wrap( { captured2.set(InboundThreadLocal.getContext().orElse(null)) }))
         executor.submit(Concurrent.wrap( { captured3.set(InboundThreadLocal.getContext().orElse(null)) }))
         executor.shutdown()
         executor.awaitTermination(5, TimeUnit.SECONDS)
      }
      then:
      captured1.get() == testContext
      captured2.get() == testContext
      captured3.get() == testContext
      InboundThreadLocal.getContext().isEmpty()
   }

   def 'double close is safe and idempotent'()
   {
      when:
      def scope = InboundThreadLocal.of(testContext)
      scope.close()
      scope.close()

      then:
      noExceptionThrown()
      InboundThreadLocal.getContext().isEmpty()
   }
}