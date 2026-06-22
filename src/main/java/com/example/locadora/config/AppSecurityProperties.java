package com.example.locadora.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private SecurityMode mode = SecurityMode.SECURE;
    private String encryptionKey = "ChangeMe-32chars-key-1234567890";

    public SecurityMode getMode() {
        return mode;
    }

    public void setMode(SecurityMode mode) {
        this.mode = mode;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isSecureMode() {
        return SecurityMode.SECURE.equals(mode);
    }

    public boolean isInsecureMode() {
        return SecurityMode.INSECURE.equals(mode);
    }
}
