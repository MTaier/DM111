package br.inatel.pos.dm111.vfp.persistence.restaurant;

import java.util.List;

public record Restaurant(String id,
        String name,
        String address,
        String userId,
        List<String> categories,
        List<Product> products) {
}