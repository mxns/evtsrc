package mxns.evtsrc;

import mxns.function.AsyncExceptionHandler;

import java.util.function.Function;

public class EventHandlerFactory<I, H, C> extends HandlerFactory<I, Event<H>, C, Void> {

    public EventHandlerFactory(
            Function<I, C> contextFactory,
            Function<I, Event<H>> payloadExtractor,
            AsyncExceptionHandler<C, Event<H>> exceptionHandler
    ) {
        super(contextFactory, payloadExtractor, exceptionHandler);
    }
}
