package com.WhoIsRoom.WhoIs_Server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WhoIsServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhoIsServerApplication.class, args);
	}

}
