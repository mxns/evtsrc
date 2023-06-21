package mxns.evtsrc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @param <C> Context type
 * @param <O> Output type
 */
public class IteratingHandlerFactory<C, O> {
    private final Function<Function<C, CompletableFuture<Void>>, CompletableFuture<Void>> transactionManager;
    private final Executor executor;


    public IteratingHandlerFactory(Function<Function<C, CompletableFuture<Void>>, CompletableFuture<Void>> transactionManager, Executor executor) {
        this.transactionManager = transactionManager;
        this.executor = executor;
    }

    public Supplier<CompletableFuture<O>> createHandler(AsyncIterator<C, Void, O> function) {
        return () -> {
            CompletableFuture<O> future = new CompletableFuture<>();
            iterate(function, future);
            return future;
        };
    }

    private void iterate(AsyncIterator<C, Void, O> function, CompletableFuture<O> future) {
        transactionManager.apply(function::next)
                .thenComposeAsync(v -> {
                    if (function.hasNext()) {
                        iterate(function, future);
                    } else {
                        future.complete(function.getResult());
                    }
                    return null;
                }, executor)
                .exceptionallyCompose(throwable -> {
                    future.completeExceptionally(throwable);
                    return null;
                });
    }
}
