package br.inatel.pos.dm111.vfa.api;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class PasswordEncryptor {

    public String encrypt(String text) {
        MessageDigest crypt = null;
        try {
            crypt = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        crypt.reset();
        crypt.update(text.getBytes(StandardCharsets.UTF_8));

        return new BigInteger(1, crypt.digest()).toString();
    }
}
