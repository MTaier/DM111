package br.inatel.pos.dm111.vfr.api.restaurant;

public record ProductResponse(String id,
                              String name,
                              String description,
                              String category,
                              float price) {
}
