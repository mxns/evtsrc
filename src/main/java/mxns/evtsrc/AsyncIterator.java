package mxns.evtsrc;

import java.util.concurrent.CompletableFuture;

/**
 * @param <C> Context type
 * @param <R> Partial result type
 * @param <O> Output type
 */
public interface AsyncIterator<C, R, O> {

    CompletableFuture<R> next(C context);

    boolean hasNext();

    O getResult();
}
