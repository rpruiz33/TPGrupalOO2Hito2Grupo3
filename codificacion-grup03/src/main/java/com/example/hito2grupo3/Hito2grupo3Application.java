package com.example.hito2grupo3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.example.hito2grupo3.entities.Festival;
import com.example.hito2grupo3.repositories.FestivalRepository;

@ConfigurationPropertiesScan
@SpringBootApplication
public class Hito2grupo3Application {

	public static void main(String[] args) {
		SpringApplication.run(Hito2grupo3Application.class, args);
	}

	@Bean
	CommandLineRunner normalizarTemporadas(FestivalRepository festivalRepository) {
		return args -> {
			var festivales = festivalRepository.findAll();

			for (Festival festival : festivales) {
				if ((festival.getTemporada() == null || festival.getTemporada().isBlank())
						&& festival.getFechaInicio() != null) {
					festival.setTemporada(Festival.determinarTemporada(festival.getFechaInicio()));
				}
			}

			if (!festivales.isEmpty()) {
				festivalRepository.saveAll(festivales);
			}
		};
	}

}
