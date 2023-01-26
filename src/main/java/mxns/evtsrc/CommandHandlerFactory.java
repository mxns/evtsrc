package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.transport.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

abstract class CommandHandlerFactory<P, I, H, C, R> {
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

    public AsyncHandler<I, List<Event<R>>> createHandler(AsyncHandlerFactory<TxContext<P, C>, H, List<Event<R>>> handlerFactory) {
        return message -> {
            AsyncHandler<P, List<Event<R>>> handler = connection -> {
                C context = contextFactory.apply(message);
                H payload = payloadExtractor.apply(message);
                AsyncHandler<H, List<Event<R>>> payloadHandler = handlerFactory.get(new TxContext<>(connection, context));
                Promise<List<Event<R>>> handleCommand;
                try {
                    handleCommand = payloadHandler.handle(payload);
                } catch (Throwable error) {
                    return exceptionHandler
                            .handle(context, payload, error)
                            .then(v -> {
                                throw new IgnoreableException(error);
                            });
                }
                if (Objects.isNull(handleCommand)) {
                    return null;
                }
                return handleCommand
                        .then(events -> insertEvents(connection, events))
                        .otherwise(error -> {
                            return exceptionHandler
                                    .handle(context, payload, error)
                                    .then(v -> {
                                        throw new IgnoreableException(error);
                                    });
                        });
            };
            return poolManager.withTransaction(handler);
        };
    }

    abstract Promise<List<Event<R>>> insertEvents(P connection, List<Event<R>> events);
}
