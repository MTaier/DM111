package br.inatel.pos.dm111.vfr.consumer;

public record Event(EventType type, UserEvent event) {

    public enum EventType {ADDED, UPDATED, DELETED};
}
