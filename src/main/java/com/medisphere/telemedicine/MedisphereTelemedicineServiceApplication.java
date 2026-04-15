package com.medisphere.telemedicine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MedisphereTelemedicineServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedisphereTelemedicineServiceApplication.class, args);
	}

}
