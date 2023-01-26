package mxns.evtsrc;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface EventHandler<E> {
    Promise<Void> handle(Event<E> query);
}
