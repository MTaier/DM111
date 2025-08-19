package br.inatel.pos.dm111.vfa.persistence.user;

public record User(String id, String name, String email, String password, UserType type) {

    public enum UserType {REGULAR, RESTAURANT}
}
