package com.sprint.part3.sb01_monew_team6.stub;

import static com.sprint.part3.sb01_monew_team6.exception.ErrorCode.*;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sprint.part3.sb01_monew_team6.exception.interest.InterestException;
import com.sprint.part3.sb01_monew_team6.exception.news.NewsException;
import com.sprint.part3.sb01_monew_team6.exception.user.UserException;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

	@GetMapping("/user")
	public void getUserException() {
		throw new UserException(USER_LOGIN_FAILED_EXCEPTION, Instant.now(), HttpStatus.UNAUTHORIZED);
	}

	@GetMapping("/interest")
	public void getInterestException() {
		throw new InterestException(INTEREST_INVALID_EXCEPTION, Instant.now(), HttpStatus.BAD_REQUEST);
	}

	@GetMapping("/news")
	public void getNewsException() {
		throw new NewsException(NEWS_INVALID_EXCEPTION, Instant.now(), HttpStatus.BAD_REQUEST);
	}
}
