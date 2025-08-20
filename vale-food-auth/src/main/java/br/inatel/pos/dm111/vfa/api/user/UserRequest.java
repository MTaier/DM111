package br.inatel.pos.dm111.vfa.api.user;

public record UserRequest(String id, String name, String email, String password, String type) {
}
