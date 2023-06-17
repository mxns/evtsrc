package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncFunction;

import java.util.List;
import java.util.function.Function;

/**
 * @param <C> Connection
 * @param <I> Input
 * @param <X> Context
 * @param <P> Parameters
 * @param <O> Output
 */
public abstract class CommandHandlerFactory<C, I, X, P, O> {
    private final ConnectionPool<C> connectionPool;
    private final HandlerFactory<I, X, P, List<Event<O>>> handlerFactory;
    private final EventMultiplexer<O> multiplexer;

    public CommandHandlerFactory(ConnectionPool<C> connectionPool, HandlerFactory<I, X, P, List<Event<O>>> handlerFactory, EventMultiplexer<O> multiplexer) {
        this.connectionPool = connectionPool;
        this.handlerFactory = handlerFactory;
        this.multiplexer = multiplexer;
    }

    abstract Promise<List<Event<O>>> insertEvents(C connection, List<Event<O>> events);

    abstract Promise<Void> publishEvents(List<Envelope<O>> envelopes);

    public AsyncFunction<I, Void> createHandler(
            Function<ConnectionContext<X, C>, AsyncFunction<P, List<Event<O>>>> factory
    ) {
        AsyncFunction<I, List<Event<O>>> handler = handlerFactory.createHandler(context -> parameters ->
                connectionPool.withTransaction(
                        connection -> factory
                                .apply(new ConnectionContext<>(context, connection))
                                .apply(parameters)
                                .then(events -> insertEvents(connection, events))
                )
        );
        return input -> handler.apply(input)
                .then(events -> publishEvents(multiplexer.mapToChannels(events)));
    }
}
