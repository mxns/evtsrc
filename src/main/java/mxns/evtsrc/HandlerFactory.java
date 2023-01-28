package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncExceptionHandler;
import mxns.function.AsyncFunction;

import java.util.Objects;
import java.util.function.Function;

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

    public AsyncFunction<I, R> createHandler(Function<C, AsyncFunction<H, R>> handlerFactory) {
        return message -> {
            C context = contextFactory.apply(message);
            H payload = payloadExtractor.apply(message);
            AsyncFunction<H, R> payloadHandler = handlerFactory.apply(context);
            Promise<R> handleCommand;
            try {
                handleCommand = payloadHandler.handle(payload);
            } catch (Throwable error) {
                return exceptionHandler
                        .handle(context, payload, error)
                        .then(v -> {
                            throw new IgnorableException(error);
                        });
            }
            if (Objects.isNull(handleCommand)) {
                return null;
            }
            return handleCommand
                    .otherwise(error -> {
                        return exceptionHandler
                                .handle(context, payload, error)
                                .then(v -> {
                                    throw new IgnorableException(error);
                                });
                    });
        };
    }
}
