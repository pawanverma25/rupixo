package dev.pawan.rupixo.vault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class VaultEncryptionConfig {

    @Value("${value.master-key}")
    private String masterKey;

    public static BytesEncryptor panEncryptor(byte[] dek){
        SecretKeySpec secretKey = new SecretKeySpec(dek, "AES");
        return new AesBytesEncryptor(secretKey, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    public BytesEncryptor dekEncryptor(){
//        byte[] masterKeyBytes = masterKey.getBytes(StandardCharsets.UTF_8);  -> need to do a base64 decode
        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
        SecretKeySpec secretKey = new SecretKeySpec(masterKeyBytes, "AES");
        return new AesBytesEncryptor(secretKey, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }
}
