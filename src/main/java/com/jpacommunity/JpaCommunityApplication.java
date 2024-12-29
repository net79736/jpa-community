package com.jpacommunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.jpacommunity.board.core.repository")
public class JpaCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaCommunityApplication.class, args);
    }

}
