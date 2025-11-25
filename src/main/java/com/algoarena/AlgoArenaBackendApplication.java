// algoarena-backend/src/main/java/com/algoarena/AlgoArenaBackendApplication.java

package com.algoarena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;  // ← ADD THIS IMPORT

@SpringBootApplication
@EnableScheduling
@EnableCaching  // ← ADD THIS ANNOTATION  and if we want custom caching strategy for diffrent diffrent services then create cacheconfig file but currently that is okey
public class AlgoArenaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlgoArenaBackendApplication.class, args);
	}
}