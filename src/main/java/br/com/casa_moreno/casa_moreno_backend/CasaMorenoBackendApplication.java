package br.com.casa_moreno.casa_moreno_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableFeignClients
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class CasaMorenoBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CasaMorenoBackendApplication.class, args);
	}

}
