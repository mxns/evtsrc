package mxns.transport;

import com.englishtown.promises.Promise;

import java.util.Objects;
import java.util.function.Function;

/**
 * Create an outer handler function around a handler factory, so that each call to the
 * outer handler produces a new inner handler. The inner handler is given access to a
 * contextualized async event bus and a contextualized logger, as well as the message payload.
 *
 * @param <I> Type handled by outer handler
 * @param <H> Type handled by inner handler
 * @param <C> Type of context
 * @param <R> Type produced by inner handler
 */
public class HandlerFactory<I, H, C, R> {
    private final Function<I, C> contextFactory;
    private final Function<I, H> payloadExtractor;
    private final AsyncExceptionHandler<C, H> exceptionHandler;

    public HandlerFactory(
            Function<I, C> contextFactory,
            Function<I, H> payloadExtractor,
            AsyncExceptionHandler<C, H> exceptionHandler
    ) {
        this.contextFactory = contextFactory;
        this.payloadExtractor = payloadExtractor;
        this.exceptionHandler = exceptionHandler;
    }

    public AsyncHandler<I, R> createHandler(AsyncHandlerFactory<C, H, R> handlerFactory) {
        return message -> {
            C context = contextFactory.apply(message);
            H payload = payloadExtractor.apply(message);
            AsyncHandler<H, R> payloadHandler = handlerFactory.get(context);
            Promise<R> promise;
            try {
                promise = payloadHandler.handle(payload);
            } catch (Throwable error) {
                return exceptionHandler
                        .handle(context, payload, error)
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
                                .handle(context, payload, error)
                                .then(v -> {
                                    throw new IgnoreableException(error);
                                });
                    });
        };
    }
}
