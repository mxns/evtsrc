package mxns.evtsrc;

public class Envelope<E> {
    public final String address;
    public final Event<E> envelope;
    public final long sequenceNumber;

    public Envelope(String address, Event<E> envelope, long sequenceNumber) {
        this.address = address;
        this.envelope = envelope;
        this.sequenceNumber = sequenceNumber;
    }
}
