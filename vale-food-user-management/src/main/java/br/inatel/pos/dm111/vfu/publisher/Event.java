package br.inatel.pos.dm111.vfu.publisher;

public record Event(EventType type, UserEvent event) {

    public enum EventType {ADDED, UPDATED, DELETED};
}
