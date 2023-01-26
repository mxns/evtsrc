package mxns.evtsrc;

import mxns.transport.AsyncHandler;
import mxns.transport.HandlerFactory;

import java.util.function.Function;

class QueryHandlerFactory<P, I, H, C, R> {
    private final ConnectionManager<P> poolManager;
    private final HandlerFactory<I, H, C, R> handlerFactory;

    QueryHandlerFactory(
            HandlerFactory<I, H, C, R> handlerFactory,
            ConnectionManager<P> poolManager
    ) {
        this.handlerFactory = handlerFactory;
        this.poolManager = poolManager;
    }

    AsyncHandler<I, R> createQueryHandler(Function<P, QueryHandler<H, R>> handlerFactory) {
        return this.handlerFactory.createHandler(
                ctx -> query ->
                        poolManager.withConnection(
                                connection -> {
                                    QueryHandler<H, R> handler = handlerFactory.apply(connection);
                                    return handler.handle(query);
                                })
        );
    }
}
