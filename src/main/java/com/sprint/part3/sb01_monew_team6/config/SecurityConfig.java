package com.sprint.part3.sb01_monew_team6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; //HttpSecurity를 사용하려면 필요
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCrypt 구현체 임포트
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 인터페이스 임포트
import org.springframework.security.web.SecurityFilterChain; // SecurityFilterChain 사용 예시

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // PasswordEncoder 빈 등록 메소드
  @Bean // 이 메소드가 반환하는 객체를 스프링 빈으로 등록
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 추가적인 Spring Security 설정 (예: HTTP 경로별 접근 제어 등)
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 서버는 보통 비활성화)
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/**").permitAll() // 모든 경로 일단 허용 (나중에 경로별 권한 설정 필요!)
            .anyRequest().authenticated()
        );

  return http.build();
  }

}