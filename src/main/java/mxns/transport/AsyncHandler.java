package mxns.transport;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncHandler<H, R> {
    Promise<R> handle(H request);
}
