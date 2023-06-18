package mxns.evtsrc;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <I> Input
 * @param <C> Context
 * @param <O> Output
 */
public class HandlerFactory<I, C, O> {
    private final BiFunction<I, Function<C, CompletableFuture<O>>, CompletableFuture<O>> contextCreator;
    private final BiFunction<C, Throwable, CompletableFuture<O>> exceptionHandler;

    public HandlerFactory(
            BiFunction<I, Function<C, CompletableFuture<O>>, CompletableFuture<O>> contextCreator,
            BiFunction<C, Throwable, CompletableFuture<O>> exceptionHandler
    ) {
        this.contextCreator = contextCreator;
        this.exceptionHandler = exceptionHandler;
    }

    public Function<I, CompletableFuture<O>> createHandler(Function<C, Supplier<CompletableFuture<O>>> handlerFactory) {
        return input -> contextCreator.apply(input, context -> apply(handlerFactory, context));
    }

    private CompletableFuture<O> apply(Function<C, Supplier<CompletableFuture<O>>> handlerFactory, C context) {
        CompletableFuture<O> future;
        try {
            future = handlerFactory.apply(context).get();
        } catch (Exception e) {
            return handleException(context, e);
        }
        return future.exceptionallyCompose(throwable -> handleException(context, throwable));
    }

    private CompletableFuture<O> handleException(C context, Throwable throwable) {
        try {
            return exceptionHandler.apply(context, throwable)
                    .thenCompose(output -> CompletableFuture.failedFuture(new IgnorableException(throwable)));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
