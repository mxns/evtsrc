package mxns.function;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncFunction<H, R> {
    Promise<R> handle(H request);
}
