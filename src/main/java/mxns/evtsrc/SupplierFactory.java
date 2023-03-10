package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncBiConsumer;
import mxns.function.AsyncSupplier;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class SupplierFactory<C, R> {
    private final Supplier<C> contextFactory;
    private final AsyncBiConsumer<C, Throwable> exceptionHandler;

    public SupplierFactory(
            Supplier<C> contextFactory,
            AsyncBiConsumer<C, Throwable> exceptionHandler
    ) {
        this.contextFactory = contextFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public AsyncSupplier<R> createHandler(Function<C, AsyncSupplier<R>> handlerFactory) {
        return () -> {
            C context = contextFactory.get();
            AsyncSupplier<R> supplier = handlerFactory.apply(context);
            Promise<R> promise;
            try {
                promise = supplier.get();
            } catch (Throwable error) {
                return exceptionHandler
                        .accept(context, error)
                        .then(v -> {
                            throw new IgnorableException(error);
                        });
            }
            if (Objects.isNull(promise)) {
                return null;
            }
            return promise
                    .otherwise(error -> {
                        return exceptionHandler
                                .accept(context, error)
                                .then(v -> {
                                    throw new IgnorableException(error);
                                });
                    });
        };
    }
}
