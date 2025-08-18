package com.yagubogu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class YaguboguApplication {

    public static void main(String[] args) {
        SpringApplication.run(YaguboguApplication.class, args);
    }
}
