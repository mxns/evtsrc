package mxns.evtsrc;

import com.englishtown.promises.Deferred;
import com.englishtown.promises.Promise;
import com.englishtown.promises.When;
import mxns.function.AsyncFunction;
import mxns.function.AsyncSupplier;

import java.util.List;
import java.util.function.Function;

/**
 * @param <C> Connection
 * @param <I> Input
 * @param <X> Context
 * @param <P> Parameters
 * @param <O> Output
 */
public abstract class StreamingCommandHandlerFactory<C, I, X, P, O> {
    private final When when;
    private final ConnectionPool<C> connectionPool;
    private final HandlerFactory<I, X, P, Void> handlerFactory;
    private final EventMultiplexer<O> multiplexer;

    public StreamingCommandHandlerFactory(When when, ConnectionPool<C> connectionPool, HandlerFactory<I, X, P, Void> handlerFactory, EventMultiplexer<O> multiplexer) {
        this.when = when;
        this.connectionPool = connectionPool;
        this.handlerFactory = handlerFactory;
        this.multiplexer = multiplexer;
    }

    abstract Promise<List<Event<O>>> insertEvents(C connection, List<Event<O>> events);

    abstract Promise<Void> publishEvents(List<Envelope<O>> envelopes);

    public AsyncFunction<I, Void> createHandler(
            Function<ConnectionContext<X, C>, AsyncFunction<P, List<Event<O>>>> factory
    ) {
        return handlerFactory.createHandler(context -> parameters ->
                connectionPool.withTransaction(
                        connection -> {
                            AsyncSupplier<Void> supplier = () -> factory.apply(new ConnectionContext<>(context, connection))
                                    .apply(parameters)
                                    .then(events -> insertEvents(connection, events))
                                    .then(events -> publishEvents(multiplexer.mapToChannels(events)));
                            return supplier.get()
                                    .then(v -> {
                                        Deferred<Void> deferred = when.defer();
                                        Task task = new Task() {
                                            @Override
                                            public Promise<Void> get() {
                                                return supplier.get();
                                            }

                                            @Override
                                            public boolean hasMore() {
                                                return Task.super.hasMore();
                                            }
                                        };
                                        repeat(task, deferred);
                                        return deferred.getPromise();
                                    });
                        }
                )
        );
    }

    private void repeat(Task task, Deferred<Void> deferred) {
        if (!task.hasMore()) {
            deferred.resolve((Void) null);
            return;
        }
        task.get()
                .then(v -> {
                    if (task.hasMore()) {
                        repeat(task, deferred);
                    } else {
                        deferred.resolve((Void) null);
                    }
                    return null;
                })
                .otherwise(error -> {
                    deferred.reject(error);
                    return null;
                });
    }

    private interface Task extends AsyncSupplier<Void> {

        default boolean hasMore() {
            return false;
        }
    }
}
