package mxns.evtsrc;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class HandlerFactoryTest {
    @Test
    public void shouldCompleteSuccessfully() throws ExecutionException, InterruptedException {
        BiFunction<Map<String, String>, Function<ContextForTesting, CompletableFuture<String>>, CompletableFuture<String>> contextCreator = (data, f) -> f.apply(new ContextForTesting(data.get("apa")));
        BiFunction<ContextForTesting, Throwable, CompletableFuture<String>> exceptionHandler = (context, throwable) -> {
            System.out.println("I got this: " + throwable.getMessage());
            return CompletableFuture.completedFuture("handled exception");
        };
        Executor executor = Runnable::run;
        HandlerFactory<Map<String, String>, ContextForTesting, String, String> factory = new HandlerFactory<>(contextCreator, CompletableFuture::completedFuture, exceptionHandler, executor);
        Function<ContextForTesting, Supplier<CompletableFuture<String>>> function = context -> () -> CompletableFuture.completedFuture(context.value);
        Function<Map<String, String>, CompletableFuture<String>> handler = factory.createHandler(function);
        CompletableFuture<String> apply = handler.apply(Map.of("apa", "giraff"));
        Assert.assertEquals("giraff", apply.get());
    }

    @Test(expected = IgnorableException.class)
    public void shouldFail() throws Throwable {
        BiFunction<Map<String, String>, Function<ContextForTesting, CompletableFuture<String>>, CompletableFuture<String>> contextCreator = (data, f) -> f.apply(new ContextForTesting(data.get("apa")));
        BiFunction<ContextForTesting, Throwable, CompletableFuture<String>> exceptionHandler = (context, throwable) -> {
            System.out.println("I got this: " + throwable.getMessage());
            return CompletableFuture.completedFuture("handled exception");
        };
        Executor executor = Runnable::run;
        HandlerFactory<Map<String, String>, ContextForTesting, String, String> factory = new HandlerFactory<>(contextCreator, CompletableFuture::completedFuture, exceptionHandler, executor);
        Function<ContextForTesting, Supplier<CompletableFuture<String>>> function = context -> () -> {
            throw new RuntimeException("Failure");
        };
        Function<Map<String, String>, CompletableFuture<String>> handler = factory.createHandler(function);
        CompletableFuture<String> apply = handler.apply(Map.of("apa", "giraff"));
        try {
            apply.get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    static class ContextForTesting {
        final String value;

        ContextForTesting(String value) {
            this.value = value;
        }
    }
}
