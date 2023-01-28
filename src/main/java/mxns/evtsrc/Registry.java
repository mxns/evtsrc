package mxns.evtsrc;

import mxns.function.AsyncFunction;

@FunctionalInterface
public interface Registry<H> {
    <R> void register(String address, AsyncFunction<H, R> handler);
}
