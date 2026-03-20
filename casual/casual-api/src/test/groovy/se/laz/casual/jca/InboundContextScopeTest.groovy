/*
 * Copyright (c) 2026, The casual project. All rights reserved.
 *
 * This software is licensed under the MIT license, https://opensource.org/licenses/MIT
 */

package se.laz.casual.jca

import se.laz.casual.api.concurrency.Concurrent
import spock.lang.Specification

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

class InboundContextScopeTest extends Specification
{
    InboundThreadContext testContext
    InboundThreadContext altContext
    SpanId testSpanId
    SpanId altSpanId
    UUID testExecution
    UUID altExecution

    def setup()
    {
        testSpanId = SpanId.of()
        altSpanId = SpanId.of()
        testExecution = UUID.randomUUID()
        altExecution = UUID.randomUUID()
        testContext = new InboundThreadContext(testSpanId, "testParent", testExecution)
        altContext = new InboundThreadContext(altSpanId, "altParent", altExecution)
    }

    def cleanup()
    {
       InboundContextScope.remove()
    }

    def "of() should reject null context with meaningful NPE"()
    {
        when:
        InboundContextScope.of(null)
        then:
        thrown NullPointerException
    }

    def "of() should accept valid context and set it in thread local"()
    {
        when:
        InboundContextScope.of(testContext)
        then:
        InboundContextScope.getContext().isPresent()
        InboundContextScope.getContext().get() == testContext
    }

    def "getContext() should return empty Optional when no context is set"()
    {
        expect:
        InboundContextScope.getContext().isEmpty()
    }

    def "getContext() should return current context when set"()
    {
        when:
        InboundContextScope.of(testContext)
        def result = InboundContextScope.getContext()

        then:
        result.isPresent()
        result.get() == testContext
    }

    def "close() should clear thread local and make getContext() return empty"()
    {
        given:
        def inboundThreadLocal = InboundContextScope.of(testContext)
        assert InboundContextScope.getContext().isPresent()

        when:
        inboundThreadLocal.close()

        then:
        InboundContextScope.getContext().isEmpty()
    }

    def "close() should be idempotent - multiple calls should be safe"()
    {
        given:
        def inboundThreadLocal = InboundContextScope.of(testContext)

        when:
        inboundThreadLocal.close()
        inboundThreadLocal.close()
        inboundThreadLocal.close()

        then:
        InboundContextScope.getContext().isEmpty()
    }

    def "try-with-resources should automatically clean up context"()
    {
        when:
        try (def ignored = InboundContextScope.of(testContext))
        {
            assert InboundContextScope.getContext().isPresent()
            assert InboundContextScope.getContext().get() == testContext
        }
        then:
        InboundContextScope.getContext().isEmpty()
    }

    def 'does not propagate to pooled thread if not wrapped - using Runnable'()
    {
       given:
       def capturedInAsync = new AtomicReference<InboundThreadContext>()
       when:
       try (def ignored = InboundContextScope.of(testContext)) {
          def executor = Executors.newSingleThreadExecutor()
          Runnable work = {
             capturedInAsync.set(InboundContextScope.getContext().orElse(null))
          }
          executor.submit (work).get(5, TimeUnit.SECONDS)
          executor.shutdown()
       }
       then:
       capturedInAsync.get() == null
       InboundContextScope.getContext().isEmpty()
    }

   def 'does propagate to pooled thread when wrapped - using Runnable'()
   {
      given:
      def capturedInAsync = new AtomicReference<InboundThreadContext>()
      when:
      try (def ignored = InboundContextScope.of(testContext))
      {
         def executor = Executors.newSingleThreadExecutor()
         Runnable work = {
            capturedInAsync.set(InboundContextScope.getContext().orElse(null))
         }
         Runnable wrapped = Concurrent.wrap(work)
         executor.submit (wrapped).get(5, TimeUnit.SECONDS)
         executor.shutdown()
      }
      then:
      capturedInAsync.get() == testContext
      InboundContextScope.getContext().isEmpty()
   }

   def 'does propagate to pooled thread when wrapped - using Callable'()
   {
      given:
      def capturedInAsync = new AtomicReference<InboundThreadContext>()
      when:
      def result
      try (def ignored = InboundContextScope.of(testContext))
      {
         def executor = Executors.newSingleThreadExecutor()
         Callable<InboundThreadContext> work = new Callable<InboundThreadContext>() {
            @Override
            InboundThreadContext call() throws Exception {
               def context = InboundContextScope.getContext().orElse(null)
               capturedInAsync.set(context)
               return context
            }
         }

         Callable<InboundThreadContext> wrappedCallable = Concurrent.wrap(work)
         def future = executor.submit(wrappedCallable)
         result = future.get(5, TimeUnit.SECONDS)
         executor.shutdown()
      }
      then:
      capturedInAsync.get() == testContext
      result == testContext
      InboundContextScope.getContext().isEmpty()
   }

   def 'wrapped Callable does nothing when no context is active'()
   {
      given:
      def captured = new AtomicReference<InboundThreadContext>()

      when:
      def executor = Executors.newSingleThreadExecutor()
      Callable<InboundThreadContext> work = new Callable<InboundThreadContext>() {
         @Override
         InboundThreadContext call() throws Exception {
            def context = InboundContextScope.getContext().orElse(null)
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
      try (def ignored = InboundContextScope.of(testContext)) {
         def executor = Executors.newSingleThreadExecutor()

         Supplier<InboundThreadContext> supplier = new Supplier<InboundThreadContext>() {
            @Override
            InboundThreadContext get() {
               def context = InboundContextScope.getContext().orElse(null)
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
      InboundContextScope.getContext().isEmpty()
   }

   def 'wrapped Supplier does nothing when no context is active'()
   {
      given:
      def captured = new AtomicReference<InboundThreadContext>()

      when:
      def executor = Executors.newSingleThreadExecutor()
      def wrapped = Concurrent.wrap({
         captured.set(InboundContextScope.getContext().orElse(null))
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
      try (def ignored = InboundContextScope.of(testContext))
      {
         def executor = Executors.newSingleThreadExecutor()
         executor.submit(Concurrent.wrap( { captured1.set(InboundContextScope.getContext().orElse(null)) }))
         executor.submit(Concurrent.wrap( { captured2.set(InboundContextScope.getContext().orElse(null)) }))
         executor.submit(Concurrent.wrap( { captured3.set(InboundContextScope.getContext().orElse(null)) }))
         executor.shutdown()
         executor.awaitTermination(5, TimeUnit.SECONDS)
      }
      then:
      captured1.get() == testContext
      captured2.get() == testContext
      captured3.get() == testContext
      InboundContextScope.getContext().isEmpty()
   }

    def 'context survives multiple pooled concurrent tasks from different outer scope'()
    {
        given:
        def captured1 = new AtomicReference<InboundThreadContext>()
        def captured2 = new AtomicReference<InboundThreadContext>()
        def captured3 = new AtomicReference<InboundThreadContext>()
        def captured4 = new AtomicReference<InboundThreadContext>()
        def captured5 = new AtomicReference<InboundThreadContext>()

        def executor = Executors.newFixedThreadPool(3)
        InboundThreadContext testContext2 = new InboundThreadContext( SpanId.of(), "bob", UUID.randomUUID(  ) )

        when:
        try (def ignored = InboundContextScope.of(testContext))
        {
            executor.submit(Concurrent.wrap( { captured1.set(InboundContextScope.getContext().orElse(null)) }))
            executor.submit(Concurrent.wrap( { captured2.set(InboundContextScope.getContext().orElse(null)) }))
        }

        try (def ignored = InboundContextScope.of(testContext2))
        {
            executor.submit(Concurrent.wrap( { captured3.set(InboundContextScope.getContext().orElse(null)) }))
            executor.submit(Concurrent.wrap( { captured4.set(InboundContextScope.getContext().orElse(null)) }))
        }

        try (def ignored = InboundContextScope.of(testContext))
        {
            executor.submit(Concurrent.wrap( { captured5.set(InboundContextScope.getContext().orElse(null)) }))
        }

        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.SECONDS)

        then:
        captured1.get() == testContext
        captured2.get() == testContext
        captured3.get() == testContext2
        captured4.get() == testContext2
        captured5.get() == testContext
        InboundContextScope.getContext().isEmpty()
    }
}