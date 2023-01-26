package mxns.evtsrc;

import com.englishtown.promises.Promise;

import java.util.List;

@FunctionalInterface
public interface TaskHandler<E> {
    Promise<List<Event<E>>> execute();
}
