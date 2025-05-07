package com.sprint.part3.sb01_monew_team6.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private static final String[] FORWARD_PATHS = {
      "/", "/login", "/signup", "/articles/**", "/interests/**", "/user-activities/**"
  };

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    for (String path : FORWARD_PATHS) {
      registry.addViewController(path)
          .setViewName("forward:/index.html");
    }
  }
}
