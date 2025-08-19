package br.inatel.pos.dm111.vfu.api.user;

public record UserRequest(String name, String email, String password, String type) {
}
