package br.inatel.pos.dm111.vfp.persistence.user;

import java.util.List;

public record User(String id,
        String name,
        String email,
        String password,
        UserType type,
        List<String> preferredCategories) {
    public enum UserType {
        REGULAR, RESTAURANT
    }
}