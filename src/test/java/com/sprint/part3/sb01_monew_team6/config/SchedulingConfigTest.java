package com.sprint.part3.sb01_monew_team6.config;

import static org.mockito.BDDMockito.then;

import com.sprint.part3.sb01_monew_team6.service.NewsCollectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SchedulingConfigTest {
  @Mock
  private NewsCollectionService service;

  @InjectMocks
  private SchedulingConfig config;


  @Test
  @DisplayName("collectNewsSchedule 호출시 collectAndSave 호출")
  void whenCollectNewsSchedule_thenCollectAndSave(){
    //when
    config.collectNewsSchedule();
    //then
    then(service).should().collectAndSave();
  }

}
