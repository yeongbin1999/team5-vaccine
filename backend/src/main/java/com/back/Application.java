package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// @WebMvcTest 컨트롤러 단위 테스트를 위해 JPA Auditing 기능을 JpaAuditingConfig을 통해 따로 활성화합니다.
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
