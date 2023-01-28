package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncFunction;
import mxns.function.AsyncSupplier;
import mxns.function.AsyncSupplierFactory;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SupplierFactory<I, C, R> {
    private final Function<I, C> contextFactory;
    private final BiFunction<C, Throwable, Promise<Void>> exceptionHandler;

    public SupplierFactory(
            Function<I, C> contextFactory,
            BiFunction<C, Throwable, Promise<Void>> exceptionHandler
    ) {
        this.contextFactory = contextFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public AsyncFunction<I, R> createSupplier(AsyncSupplierFactory<C, R> handlerFactory) {
        return message -> {
            C context = contextFactory.apply(message);
            AsyncSupplier<R> payloadHandler = handlerFactory.get(context);
            Promise<R> promise;
            try {
                promise = payloadHandler.get();
            } catch (Throwable error) {
                return exceptionHandler
                        .apply(context, error)
                        .then(v -> {
                            throw new IgnoreableException(error);
                        });
            }
            if (Objects.isNull(promise)) {
                return null;
            }
            return promise
                    .otherwise(error -> {
                        return exceptionHandler
                                .apply(context, error)
                                .then(v -> {
                                    throw new IgnoreableException(error);
                                });
                    });
        };
    }
}