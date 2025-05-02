package com.sprint.part3.sb01_monew_team6.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CommentTest {

  @DisplayName("markDeleted() 호출시 값이 true 변경 테스트")
  @Test
  public void markDeleted_setsIsDeletedTrue(){
    //given
    Comment comment = Comment.builder()
        .isDeleted(false).build();

    //when
    comment.markDeleted();

    //then
    assertThat(comment.isDeleted()).isTrue();
  }
}