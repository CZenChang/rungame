package com.dodognoman.rungame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class RungameApplication {

	public static void main(String[] args) {
		SpringApplication.run(RungameApplication.class, args);
	}

}
