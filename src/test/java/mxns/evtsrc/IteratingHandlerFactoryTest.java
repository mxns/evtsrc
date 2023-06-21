package mxns.evtsrc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class IteratingHandlerFactoryTest {
    @Test
    public void shouldCompleteSuccessfully() throws ExecutionException, InterruptedException {
        BiFunction<Map<String, String>, Function<HandlerFactoryTest.ContextForTesting, CompletableFuture<String>>, CompletableFuture<String>> contextCreator = (data, f) -> f.apply(new HandlerFactoryTest.ContextForTesting(data.get("apa")));
        BiFunction<HandlerFactoryTest.ContextForTesting, Throwable, CompletableFuture<String>> exceptionHandler = (context, throwable) -> {
            System.out.println("I got this: " + throwable.getMessage());
            return CompletableFuture.completedFuture("handled exception");
        };
        Executor executor = Runnable::run;
        Map<String, String> ctx = Map.of("apa", "giraff");
        Function<Map<String, String>, String> f = context -> "result";
        Function<Function<Map<String, String>, CompletableFuture<Void>>, CompletableFuture<Void>> transactionManager = function -> function.apply(ctx);
        IteratingHandlerFactory<Map<String, String>, AtomicInteger> iterFactory = new IteratingHandlerFactory<>(transactionManager, executor);
        AtomicInteger counter = new AtomicInteger(0);
        AsyncIterator<Map<String, String>, Void, AtomicInteger> iterator = new AsyncIterator<>() {
            @Override
            public CompletableFuture<Void> next(Map<String, String> context) {
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public boolean hasNext() {
                return counter.getAndIncrement() < 2;
            }

            @Override
            public AtomicInteger getResult() {
                return counter;
            }
        };
        Supplier<CompletableFuture<AtomicInteger>> handler = iterFactory.createHandler(iterator);
        CompletableFuture<AtomicInteger> apply = handler.get();
        Assert.assertEquals(3, apply.get().get());
    }
}
