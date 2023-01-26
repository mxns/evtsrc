package mxns.transport;

@FunctionalInterface
public interface Registry<H> {
    <R> void register(String address, AsyncHandler<H, R> handler);
}
