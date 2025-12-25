package com.fintech.digiwallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DigiwalletApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigiwalletApplication.class, args);
	}

}
