package br.inatel.pos.dm111.vfr.persistence.restaurant;

public record Product(String id,
                      String name,
                      String description,
                      String category,
                      float price) {
}
