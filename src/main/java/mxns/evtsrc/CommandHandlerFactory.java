package mxns.evtsrc;

import mxns.function.AsyncFunction;

import java.util.List;
import java.util.function.BiFunction;

public class CommandHandlerFactory<P, I, C, H, R> {
    private final ConnectionPool<P> connectionPool;
    private final HandlerFactory<I, C, H, List<Event<R>>> handlerFactory;

    protected CommandHandlerFactory(
            HandlerFactory<I, C, H, List<Event<R>>> handlerFactory,
            ConnectionPool<P> connectionPool
    ) {
        this.handlerFactory = handlerFactory;
        this.connectionPool = connectionPool;
    }

    public AsyncFunction<I, List<Event<R>>> createHandler(BiFunction<P, C, AsyncFunction<H, List<Event<R>>>> handlerFactory) {
        return this.handlerFactory.createHandler(context -> message ->
                connectionPool.withTransaction(
                        connection -> handlerFactory.apply(connection, context).handle(message)
                ));
    }
}
