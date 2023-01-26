package mxns.evtsrc;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface QueryHandler<I, R> {
    Promise<R> handle(I query);
}
