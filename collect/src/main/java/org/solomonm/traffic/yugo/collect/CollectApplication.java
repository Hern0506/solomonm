package org.solomonm.traffic.yugo.collect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CollectApplication {

	public static void main(String[] args) {
		SpringApplication.run(CollectApplication.class, args);
	}

}
