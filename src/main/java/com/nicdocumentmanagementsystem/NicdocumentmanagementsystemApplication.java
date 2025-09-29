package com.nicdocumentmanagementsystem;

import com.nicdocumentmanagementsystem.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class NicdocumentmanagementsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(NicdocumentmanagementsystemApplication.class, args);
	}

}