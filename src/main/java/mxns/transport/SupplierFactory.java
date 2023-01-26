package mxns.transport;

import com.englishtown.promises.Promise;
import com.englishtown.promises.When;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Create an outer handler function around a supplier factory, so that each call to the
 * outer handler produces a new inner supplier. The inner supplier is given access to a
 * context, as well as the message payload.
 *
 * @param <I> Type handled by outer handler
 * @param <C> Type of context
 * @param <R> Type produced by inner handler
 */
public class SupplierFactory<I, C, R> {
    private final When when;
    private final Function<I, C> contextFactory;
    private final BiFunction<C, Throwable, Promise<R>> exceptionHandler;

    public SupplierFactory(
            When when,
            Function<I, C> contextFactory,
            BiFunction<C, Throwable, Promise<R>> exceptionHandler
    ) {
        this.when = when;
        this.contextFactory = contextFactory;
        this.exceptionHandler = exceptionHandler;
    }

    public AsyncHandler<I, R> createSupplier(AsyncSupplierFactory<C, R> handlerFactory) {
        return message -> {
            C context = contextFactory.apply(message);
            AsyncSupplier<R> payloadHandler = handlerFactory.get(context);
            Promise<R> promise;
            try {
                promise = payloadHandler.get();
            } catch (Throwable error) {
                return exceptionHandler.apply(context, error);
            }
            if (Objects.isNull(promise)) {
                return when.resolve(null);
            }
            return promise.otherwise(error -> exceptionHandler.apply(context, error));
        };
    }
}
