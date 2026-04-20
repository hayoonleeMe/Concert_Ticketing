package com.concertticket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// @SpringBootApplication에서 분리 — @WebMvcTest 슬라이스에서 JPA 메타모델 초기화 방지
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
