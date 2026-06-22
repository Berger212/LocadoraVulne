package com.example.locadora.util;

import com.example.locadora.config.AppSecurityProperties;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class InputSanitizer {

    private final AppSecurityProperties securityProperties;

    public InputSanitizer(AppSecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String sanitize(String value) {
        if (value == null || securityProperties.isInsecureMode()) {
            return value;
        }
        return Jsoup.clean(value, Safelist.basic());
    }
}
