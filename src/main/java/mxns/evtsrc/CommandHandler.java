package mxns.evtsrc;

import com.englishtown.promises.Promise;

import java.util.List;

@FunctionalInterface
public interface CommandHandler<H, E> {
    Promise<List<Event<E>>> handle(H command);
}
