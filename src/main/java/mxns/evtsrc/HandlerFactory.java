package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncFunction;

import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @param <I> Input
 * @param <X> Context
 * @param <P> Parameters
 * @param <O> Output
 */
public class HandlerFactory<I, X, P, O> {
    private final Function<I, X> contextExtractor;
    private final Function<I, P> parameterExtractor;
    private final AsyncExceptionHandler<X, P> exceptionHandler;

    public HandlerFactory(
            Function<I, X> contextExtractor,
            Function<I, P> parameterExtractor,
            AsyncExceptionHandler<X, P> exceptionHandler
    ) {
        this.contextExtractor = contextExtractor;
        this.parameterExtractor = parameterExtractor;
        this.exceptionHandler = exceptionHandler;
    }

    public AsyncFunction<I, O> createHandler(Function<X, AsyncFunction<P, O>> factory) {
        return input -> {
            X context = contextExtractor.apply(input);
            P parameters = parameterExtractor.apply(input);
            AsyncFunction<P, O> function = factory.apply(context);
            Promise<O> promise;
            try {
                promise = function.apply(parameters);
            } catch (Throwable error) {
                return exceptionHandler
                        .handle(context, parameters, error)
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
                                .handle(context, parameters, error)
                                .then(v -> {
                                    throw new IgnorableException(error);
                                });
                    });
        };
    }
}
