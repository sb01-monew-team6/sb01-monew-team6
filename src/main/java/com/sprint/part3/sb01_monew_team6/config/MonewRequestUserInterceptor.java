package com.sprint.part3.sb01_monew_team6.config;

import com.sprint.part3.sb01_monew_team6.exception.user.UserNotFoundException;
import com.sprint.part3.sb01_monew_team6.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.bind.MissingRequestHeaderException;

@Component
public class MonewRequestUserInterceptor implements HandlerInterceptor {

  private final UserRepository userRepository;

  public MonewRequestUserInterceptor(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response,
      Object handler) throws Exception {


    String header = request.getHeader("Monew-Request-User-ID");
    if (header == null) {
      throw new MissingRequestHeaderException("Monew-Request-User-ID", null);
    }
    long userId;
    try {
      userId = Long.parseLong(header);
    } catch (NumberFormatException ex) {
      throw new MissingRequestHeaderException("Monew-Request-User-ID", null);
    }

    userRepository.findById(userId)
        .filter(u -> !u.isDeleted())
        .orElseThrow(() -> new UserNotFoundException(userId));

    request.setAttribute("userId", userId);
    return true;
  }
}