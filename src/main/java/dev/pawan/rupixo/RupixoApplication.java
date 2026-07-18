package dev.pawan.rupixo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RupixoApplication {

	public static void main(String[] args) {
		SpringApplication.run(RupixoApplication.class, args);
	}

}
