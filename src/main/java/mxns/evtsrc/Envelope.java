package mxns.evtsrc;

public record Envelope<E>(String address, Event<E> envelope, long sequenceNumber) {
}
