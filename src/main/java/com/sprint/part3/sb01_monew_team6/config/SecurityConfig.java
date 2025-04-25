package com.sprint.part3.sb01_monew_team6.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

        // --- 명시적 예외 처리 핸들러 설정 ---
        .exceptionHandling(ex -> ex
            // 인증 실패 시 (로그인 필요 등) 401 반환
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            // 인가 실패 시 (권한 부족 등) 403 반환
            .accessDeniedHandler((request, response, accessDeniedException) ->
                response.setStatus(HttpServletResponse.SC_FORBIDDEN)
            )
        )

        // --- 경로별 인가 설정 ---
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/v1/test/**").permitAll() // 테스트용 경로는 인증 없이 모두 허용
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원가입
            .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll() // 로그인
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return username -> {
      throw new UsernameNotFoundException("User not found: " + username + " (Dummy Service)");
    };
  }
}