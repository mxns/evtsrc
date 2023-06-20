package mxns.evtsrc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <I> Input type
 * @param <C> Context type
 * @param <R> Handler return type
 * @param <O> Output type
 */
public class HandlerFactory<I, C, R, O> {
    private final BiFunction<I, Function<C, CompletableFuture<O>>, CompletableFuture<O>> deserializer;
    private final Function<R, CompletableFuture<O>> serializer;
    private final BiFunction<C, Throwable, CompletableFuture<O>> exceptionHandler;
    private final Executor executor;

    public HandlerFactory(
            BiFunction<I, Function<C, CompletableFuture<O>>, CompletableFuture<O>> deserializer,
            Function<R, CompletableFuture<O>> serializer,
            BiFunction<C, Throwable, CompletableFuture<O>> exceptionHandler,
            Executor executor
    ) {
        this.deserializer = deserializer;
        this.serializer = serializer;
        this.exceptionHandler = exceptionHandler;
        this.executor = executor;
    }

    public Function<I, CompletableFuture<O>> createHandler(Function<C, Supplier<CompletableFuture<R>>> factory) {
        return input -> deserializer.apply(input, context -> apply(factory, context));
    }

    private CompletableFuture<O> apply(Function<C, Supplier<CompletableFuture<R>>> factory, C context) {
        CompletableFuture<O> future;
        try {
            future = factory.apply(context).get().thenComposeAsync(serializer, executor);
        } catch (Exception e) {
            return handleException(context, e);
        }
        return future.exceptionallyCompose(throwable -> handleException(context, throwable));
    }

    private CompletableFuture<O> handleException(C context, Throwable throwable) {
        try {
            return exceptionHandler.apply(context, throwable)
                    .thenComposeAsync(output -> CompletableFuture.failedFuture(new IgnorableException(throwable)), executor);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
