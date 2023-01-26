package mxns.evtsrc;

import java.util.UUID;

public record Event<P>(int type, UUID aggregateId, P payload, long timestamp, String source, String version) {
}
