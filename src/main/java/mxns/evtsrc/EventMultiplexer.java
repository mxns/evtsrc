package mxns.evtsrc;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class EventMultiplexer<E> {

    private final Map<Integer, List<Channel>> channels = new HashMap<>();

    public void addChannel(Set<Integer> eventTypes, String channelAddress) {
        Channel channel = new Channel(channelAddress, -1);
        eventTypes.forEach(eventType -> this.channels.computeIfAbsent(eventType, k -> new ArrayList<>()).add(channel));
    }

    public List<Envelope<E>> mapToChannels(List<Event<E>> events) {
        return events
                .stream()
                .flatMap(event ->
                        channels.getOrDefault(event.type(), Collections.emptyList())
                                .stream()
                                .map(channel -> new Envelope<>(channel.channelAddress, event, channel.sequenceNumber.incrementAndGet()))
                )
                .collect(Collectors.toList());
    }

    private static class Channel {
        private final AtomicLong sequenceNumber;
        private final String channelAddress;

        private Channel(String channelAddress, long sequenceNumber) {
            this.channelAddress = channelAddress;
            this.sequenceNumber = new AtomicLong(sequenceNumber);
        }
    }
}
