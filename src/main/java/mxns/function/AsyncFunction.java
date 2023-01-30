package mxns.function;

import com.englishtown.promises.Promise;

@FunctionalInterface
public interface AsyncFunction<T, R> {
    Promise<R> apply(T request);
}
