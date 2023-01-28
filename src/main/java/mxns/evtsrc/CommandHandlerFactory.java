package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.*;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CommandHandlerFactory<P, I, H, C, R> {
    private final ConnectionManager<P> poolManager;
    private final Function<I, C> contextFactory;
    private final Function<I, H> payloadExtractor;
    private final AsyncExceptionHandler<C, H> exceptionHandler;

    protected CommandHandlerFactory(
            Function<I, C> contextFactory,
            Function<I, H> payloadExtractor,
            AsyncExceptionHandler<C, H> exceptionHandler,
            ConnectionManager<P> poolManager
    ) {
        this.poolManager = poolManager;
        this.contextFactory = contextFactory;
        this.payloadExtractor = payloadExtractor;
        this.exceptionHandler = exceptionHandler;
    }

    public AsyncFunction<I, List<Event<R>>> createHandler(BiFunction<P, C, AsyncFunction<H, List<Event<R>>>> handlerFactory) {
        return message -> {
            AsyncFunction<P, List<Event<R>>> handler = connection -> {
                C context = contextFactory.apply(message);
                H payload = payloadExtractor.apply(message);
                AsyncFunction<H, List<Event<R>>> payloadHandler = handlerFactory.apply(connection, context);
                Promise<List<Event<R>>> handleCommand;
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
            return poolManager.withTransaction(handler);
        };
    }
}
