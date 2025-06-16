package br.com.casa_moreno.casa_moreno_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CasaMorenoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasaMorenoBackendApplication.class, args);
	}

}
