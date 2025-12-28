package com.fintech.digiwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class DigiwalletApplication {

	public static void main(String[] args) {
		System.out.println("STARTING APPLICATION");
		SpringApplication.run(DigiwalletApplication.class, args);
	}
}