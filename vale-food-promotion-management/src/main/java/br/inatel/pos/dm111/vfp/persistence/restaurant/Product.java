package br.inatel.pos.dm111.vfp.persistence.restaurant;

public record Product(String id,
        String name,
        String description,
        String category,
        Double price) {
}