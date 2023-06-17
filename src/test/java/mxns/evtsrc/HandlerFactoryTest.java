package mxns.evtsrc;

import org.junit.Test;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class HandlerFactoryTest {
    @Test
    public void test() throws ExecutionException, InterruptedException {
        Function<Map<String, String>, CompletableFuture<Context>> contextCreator = data -> CompletableFuture.completedFuture(new Context(data.get("apa")));
        BiFunction<Context, Throwable, CompletableFuture<String>> exceptionHandler = (context, throwable) -> {
            System.out.println("I got this: " + throwable.getMessage());
            return CompletableFuture.completedFuture("handled exception");
        };
        BiFunction<Context, Throwable, CompletableFuture<String>> failedException = (context, throwable) -> {
            //return CompletableFuture.failedFuture(new RuntimeException("Failure in exception handler"));
            throw new RuntimeException("Failure in exception handler");
        };
        HandlerFactory<Map<String, String>, Context, String> factory = new HandlerFactory<>(contextCreator, failedException);
        Function<Context, Supplier<CompletableFuture<String>>> function = context -> () -> CompletableFuture.completedFuture(context.value);
        Function<Context, Supplier<CompletableFuture<String>>> failed = context -> () -> {
            return CompletableFuture.failedFuture(new RuntimeException("Failure"));
            //throw new RuntimeException("Failure");
        };
        Function<Map<String, String>, CompletableFuture<String>> handler = factory.createHandler(function);
        CompletableFuture<String> apply = handler.apply(Map.of("apa", "giraff"));
        System.out.println(apply.get());
    }

    static class Context {
        final String value;

        Context(String value) {
            this.value = value;
        }
    }
}
