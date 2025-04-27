package com.sprint.part3.sb01_monew_team6.config;

import com.sprint.part3.sb01_monew_team6.entity.Interest;
import com.sprint.part3.sb01_monew_team6.repository.InterestRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")        // 개발(dev) 프로파일에서만 실행
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

  private final InterestRepository interestRepository;

  @Override
  public void run(ApplicationArguments args) {
    // 이미 데이터가 들어있으면 아무 것도 안 함
    if (interestRepository.count() > 0) {
      log.info(">>> DataInitializer: 이미 관심사 데이터가 존재하므로 초기 로딩을 건너뜁니다.");
      return;
    }

    // 샘플 관심사 3건 생성
    Interest sports = new Interest();
    sports.setName("스포츠");
    sports.setKeyword(List.of("축구", "야구", "농구"));

    Interest politics = new Interest();
    politics.setName("정치");
    politics.setKeyword(List.of("대통령", "국회", "선거"));

    Interest economy = new Interest();
    economy.setName("경제");
    economy.setKeyword(List.of("주식", "환율", "금리"));

    interestRepository.saveAll(List.of(sports, politics, economy));

    log.info(">>> DataInitializer: 샘플 관심사 3건을 자동 등록했습니다.");
  }
}