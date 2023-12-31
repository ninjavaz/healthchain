package com.healthchain.backend.model.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "network")
@Data
public class NetworkProperties {

    private String channel;
    private String contract;

    private Map<String, HospInfo> hospInfoByName;

    @Data
    public static class HospInfo {
        private String caUrl;
        private String certPath;
        private String networkConfigPath;
        private String adminUsername;
        private String adminPassword;
        private String mspName;
    }
}
