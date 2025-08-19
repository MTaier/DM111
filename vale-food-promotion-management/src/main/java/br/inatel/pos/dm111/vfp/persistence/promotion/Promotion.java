package br.inatel.pos.dm111.vfp.persistence.promotion;

public record Promotion(String id,
        String restaurantId,
        String title,
        String description,
        String category,
        Double price) {
}