package br.inatel.pos.dm111.vfu.api.user;

import java.math.BigDecimal;

public record PromotionDto(
        String id,
        String restaurantId,
        String title,
        String description,
        String category,
        BigDecimal price,
        Boolean active) {
}
