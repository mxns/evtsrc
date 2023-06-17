package mxns.evtsrc;

import java.util.Objects;
import java.util.UUID;

public final class Event<P> {
    private final int type;
    private final UUID aggregateId;
    private final P payload;
    private final long timestamp;
    private final String source;
    private final String version;

    public Event(int type, UUID aggregateId, P payload, long timestamp, String source, String version) {
        this.type = type;
        this.aggregateId = aggregateId;
        this.payload = payload;
        this.timestamp = timestamp;
        this.source = source;
        this.version = version;
    }

    public int type() {
        return type;
    }

    public UUID aggregateId() {
        return aggregateId;
    }

    public P payload() {
        return payload;
    }

    public long timestamp() {
        return timestamp;
    }

    public String source() {
        return source;
    }

    public String version() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Event) obj;
        return this.type == that.type &&
                Objects.equals(this.aggregateId, that.aggregateId) &&
                Objects.equals(this.payload, that.payload) &&
                this.timestamp == that.timestamp &&
                Objects.equals(this.source, that.source) &&
                Objects.equals(this.version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, aggregateId, payload, timestamp, source, version);
    }

    @Override
    public String toString() {
        return "Event[" +
                "type=" + type + ", " +
                "aggregateId=" + aggregateId + ", " +
                "payload=" + payload + ", " +
                "timestamp=" + timestamp + ", " +
                "source=" + source + ", " +
                "version=" + version + ']';
    }

}
