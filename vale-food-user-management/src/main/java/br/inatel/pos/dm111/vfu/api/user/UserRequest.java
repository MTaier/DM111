package br.inatel.pos.dm111.vfu.api.user;

import java.util.List;

public record UserRequest(String name, String email, String password, String type, List<String> preferredCategories) {
}
