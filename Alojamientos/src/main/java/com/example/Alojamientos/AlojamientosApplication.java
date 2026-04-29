package com.example.Alojamientos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // Habilita los @Scheduled jobs (ReservaScheduler)
public class AlojamientosApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlojamientosApplication.class, args);
	}

}