package mxns.evtsrc;

import com.englishtown.promises.Promise;
import mxns.function.AsyncFunction;

import java.util.List;
import java.util.function.BiFunction;

public abstract class SqlHandlerRegistry<P, I, C, H, R> {
    private final CommandHandlerFactory<P, I, C, H, R> commandHandlers;
    private final EventMultiplexer<R> multiplexer;

    public SqlHandlerRegistry(
            CommandHandlerFactory<P, I, C, H, R> commandHandlers,
            EventMultiplexer<R> multiplexer
    ) {
        this.commandHandlers = commandHandlers;
        this.multiplexer = multiplexer;
    }

    abstract Promise<List<Event<R>>> insertEvents(P connection, List<Event<R>> events);

    abstract Promise<Void> publishEvents(List<Envelope<R>> envelopes);

    public AsyncFunction<I, Void> createCommandHandler(BiFunction<P, C, AsyncFunction<H, List<Event<R>>>> handlerFactory) {
        AsyncFunction<I, List<Event<R>>> handler = commandHandlers.createHandler(
                (connection, context) -> payload ->
                        handlerFactory
                                .apply(connection, context)
                                .apply(payload)
                                .then(events -> insertEvents(connection, events))
        );
        return message -> handler.apply(message)
                .then(events -> publishEvents(multiplexer.mapToChannels(events)));
    }
}
