package com.sprint.part3.sb01_monew_team6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus; // HttpStatus 임포트 추가
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // SessionCreationPolicy 임포트 추가
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint; // AuthenticationEntryPoint 임포트
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler; // AccessDeniedHandler 임포트
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @Order(1)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // --- 명시적 예외 처리 핸들러 설정 (수정) ---
        .exceptionHandling(ex -> ex
            // 인증 실패 시 401 반환
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            // 인가 실패 시 직접 403 상태 코드 설정
            .accessDeniedHandler((request, response, accessDeniedException) ->
                response.setStatus(HttpServletResponse.SC_FORBIDDEN) // <<<--- 여기를 수정!
            )
        )
        // ----------------------------------------

        .authorizeHttpRequests(authz -> authz
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
            .anyRequest().authenticated()
        );

    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    // 가짜 UserDetailsService 빈 유지
    return username -> {
      throw new UsernameNotFoundException("User not found: " + username + " (Dummy Service)");
    };
  }
}