package br.inatel.pos.dm111.vfa.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Configuration
public class JWTConfiguration {

    @Value("${vale-food.auth.private.key}")
    private String privateKey;

    @Bean
    public PrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        var keyBytes = Base64.getDecoder().decode(privateKey);
        var keySpec = new PKCS8EncodedKeySpec(keyBytes);
        var keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePrivate(keySpec);
    }
}
