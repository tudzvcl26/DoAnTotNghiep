package com.recruitment.recruitmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RecruitmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentServiceApplication.class, args);
    }

}