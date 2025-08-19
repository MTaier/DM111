package br.inatel.pos.dm111.vfa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class KeyPairGeneratorUtil {

    private static final Logger log = LoggerFactory.getLogger(KeyPairGeneratorUtil.class);

    public static void main(String[] args) throws NoSuchAlgorithmException {

        // Start Key Pair Generator
        var keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);

        // Start Key Pair
        var keyPair = keyPairGen.generateKeyPair();
        var publicKey = keyPair.getPublic();
        var privateKey = keyPair.getPrivate();

        // Encode the keys as string
        var publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        var privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());

        log.info("Public key: {}", publicKeyStr);
        log.info("Private Key: {}", privateKeyStr);
    }
}
