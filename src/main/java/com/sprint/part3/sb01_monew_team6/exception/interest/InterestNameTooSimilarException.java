package com.sprint.part3.sb01_monew_team6.exception.interest;

import com.sprint.part3.sb01_monew_team6.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import java.time.Instant;

// InterestException 상속 (유사 이름 예외)
public class InterestNameTooSimilarException extends InterestException {

  // 생성자: 유사한 이름과 기존 이름을 메시지에 포함할 수도 있음 (여기서는 간단히 ErrorCode 사용)
  public InterestNameTooSimilarException(String newName, String existingName, double similarity) {
    super(ErrorCode.INTEREST_NAME_TOO_SIMILAR, Instant.now(), HttpStatus.CONFLICT); // 409 Conflict 사용 (리소스 충돌의 일종으로 간주)
    // 필요시 구체적인 정보를 로깅하거나 메시지에 포함
    // log.warn("Interest name '{}' is too similar to existing name '{}' (Similarity: {})", newName, existingName, similarity);
  }

  // 간단한 버전의 생성자 (메시지만 사용)
  public InterestNameTooSimilarException() {
    super(ErrorCode.INTEREST_NAME_TOO_SIMILAR, Instant.now(), HttpStatus.CONFLICT);
  }
}