package br.inatel.pos.dm111.vfr.api.core.interceptor;

public record AppJwtToken(String issuer,
                          String subject,
                          String role,
                          String method,
                          String uri) {
}
