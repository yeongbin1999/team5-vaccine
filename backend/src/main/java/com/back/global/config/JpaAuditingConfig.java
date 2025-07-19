package com.back.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing // JPA Auditing 기능을 여기서 활성화합니다.
public class JpaAuditingConfig {
    // 필요한 경우, Auditing 관련 추가적인 Bean 정의를 이곳에 할 수 있습니다.
    // 예를 들어, @CreatedBy, @LastModifiedBy 필드에 사용자 ID 등을 주입하려면
    // AuditorAware 인터페이스 구현체를 여기에 Bean으로 등록할 수 있습니다.

    // 단위 테스트 시
    //  @WebMvcTest(controllers = CartController.class,
    //        excludeFilters = {
    //                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JpaAuditingConfig.class)
    //        })
    //  class CartControllerTest { }
    // 이런 식으로 JpaAuditingConfig.class을 비활성화하여 @WebMvcTest 어노테이션을 사용하면
    // 웹 계층 테스트에 필요한 최소한의 빈만 로드하고, JPA Auditing 관련
    // 빈 로딩 시 발생하는 오류를 피할 수 있습니다
}