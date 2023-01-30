package mxns.evtsrc;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncExceptionHandler<C, H> {
    Promise<Void> handle(C context, H payload, Throwable throwable);
}
