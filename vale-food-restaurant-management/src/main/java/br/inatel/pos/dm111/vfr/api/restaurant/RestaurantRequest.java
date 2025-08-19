package br.inatel.pos.dm111.vfr.api.restaurant;

import java.util.List;

public record RestaurantRequest(String name,
                                String address,
                                String userId,
                                List<String> categories,
                                List<ProductRequest> products) {
}
