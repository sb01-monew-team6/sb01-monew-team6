package com.sprint.part3.sb01_monew_team6.config;

import static org.mockito.BDDMockito.then;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SchedulingConfigTest {
  @Mock
  NewsCollectionService service;

  private SchedulingConfig config;

  @BeforeEach
  void setUp(){
    MockitoAnnotations.openMocks(this);
    config = new SchedulingConfig(service);
  }

  @Test
  @DisplayName("collectNewsSchedule 호출시 collectAndSave 호출")
  void whenCollectNewsSchedule_thenCollectAndSave(){
    //when
    config.collectNewsSchedule();
    //then
    then(service).should().collectAndSave();
  }

}
