package com.healthchain.backend;

import com.healthchain.backend.model.util.NetworkProperties;
import com.healthchain.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
//@EnableConfigurationProperties(NetworkProperties.class)
public class BackendApplication {

    static {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner enrollAdmins(AuthService authService) {
        return args -> {
            authService.enrollAdmin("hosp1");
            authService.enrollAdmin("hosp2");
        };
    }


}
