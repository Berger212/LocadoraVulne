package com.example.locadora.util;

import com.example.locadora.config.AppSecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class DataProtectionService {

    private static final String AES = "AES";
    private final AppSecurityProperties securityProperties;

    public DataProtectionService(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String protect(String plainText) {
        if (plainText == null || securityProperties.isInsecureMode()) {
            return plainText;
        }
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, buildKey());
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao cifrar documento", e);
        }
    }

    public String reveal(String storedValue) {
        if (storedValue == null || securityProperties.isInsecureMode()) {
            return storedValue;
        }
        try {
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, buildKey());
            byte[] decoded = Base64.getDecoder().decode(storedValue);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao decifrar documento", e);
        }
    }

    private SecretKeySpec buildKey() {
        byte[] keyBytes = securityProperties.getEncryptionKey()
                .getBytes(StandardCharsets.UTF_8);
        byte[] fixed = new byte[16];
        System.arraycopy(keyBytes, 0, fixed, 0, Math.min(keyBytes.length, 16));
        return new SecretKeySpec(fixed, AES);
    }
}
