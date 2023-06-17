package mxns.evtsrc;

import java.util.Objects;

public final class Envelope<E> {
    private final String address;
    private final Event<E> envelope;
    private final long sequenceNumber;

    public Envelope(String address, Event<E> envelope, long sequenceNumber) {
        this.address = address;
        this.envelope = envelope;
        this.sequenceNumber = sequenceNumber;
    }

    public String address() {
        return address;
    }

    public Event<E> envelope() {
        return envelope;
    }

    public long sequenceNumber() {
        return sequenceNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Envelope) obj;
        return Objects.equals(this.address, that.address) &&
                Objects.equals(this.envelope, that.envelope) &&
                this.sequenceNumber == that.sequenceNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, envelope, sequenceNumber);
    }

    @Override
    public String toString() {
        return "Envelope[" +
                "address=" + address + ", " +
                "envelope=" + envelope + ", " +
                "sequenceNumber=" + sequenceNumber + ']';
    }

}
