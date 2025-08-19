package br.inatel.pos.dm111.vfu.config;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JWTConfiguration {

    @Value("${vale-food.auth.public.key}")
    private String publicKey;

    @Bean
    public PublicKey loadPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        var keyBytes = Base64.getDecoder().decode(publicKey);
        var keySpec = new X509EncodedKeySpec(keyBytes);
        var keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }

    @Bean
    public JwtParser jwtParser(PublicKey publicKey) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build();
    }
}
