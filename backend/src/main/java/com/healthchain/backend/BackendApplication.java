package com.healthchain.backend;

import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class BackendApplication {

	static {
		System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
	}

	public static void main(String[] args) {
		// Configure sdk
		OpenTelemetrySdk sdk = OpenTelemetrySdk.builder().build();
		// Shutdown sdk
		sdk.getSdkLoggerProvider().shutdown().join(2, TimeUnit.SECONDS);
		sdk.getSdkTracerProvider().shutdown().join(2, TimeUnit.SECONDS);
		sdk.getSdkMeterProvider().shutdown().join(2, TimeUnit.SECONDS);

		SpringApplication.run(BackendApplication.class, args);
	}

}
