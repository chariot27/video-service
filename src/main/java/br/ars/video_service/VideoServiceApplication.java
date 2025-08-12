package br.ars.video_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import br.ars.video_service.config.BunnyStreamProps;

@SpringBootApplication
@EnableConfigurationProperties(BunnyStreamProps.class)
public class VideoServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoServiceApplication.class, args);
	}

}
