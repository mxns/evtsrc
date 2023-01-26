package mxns.transport;

public record TxContext<P, C>(P connection, C context) {
}
