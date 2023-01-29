package mxns.evtsrc;

import mxns.function.AsyncFunction;

import java.util.function.BiFunction;

class QueryHandlerFactory<P, I, H, C, R> {
    private final ConnectionPool<P> connectionPool;
    private final HandlerFactory<I, H, C, R> handlerFactory;

    QueryHandlerFactory(
            HandlerFactory<I, H, C, R> handlerFactory,
            ConnectionPool<P> connectionPool
    ) {
        this.handlerFactory = handlerFactory;
        this.connectionPool = connectionPool;
    }

    AsyncFunction<I, R> createHandler(BiFunction<P, C, AsyncFunction<H, R>> handlerFactory) {
        return this.handlerFactory.createHandler(context -> message ->
                connectionPool.withConnection(
                        connection -> handlerFactory.apply(connection, context).handle(message))
        );
    }
}
