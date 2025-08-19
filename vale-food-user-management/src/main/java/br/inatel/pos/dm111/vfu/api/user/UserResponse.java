package br.inatel.pos.dm111.vfu.api.user;

import java.util.List;

public record UserResponse(String id, String name, String email, String type, List<String> preferredCategories) {
}
