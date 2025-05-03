package com.sprint.part3.sb01_monew_team6.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
    comment.softDelete();

    //then
    assertThat(comment.isDeleted()).isTrue();
  }

  @DisplayName("updateContent - content만 수정됨을 검증")
  @Test
  void updateContent_shouldChangeOnlyContent() {
    // given
    Comment comment = Comment.builder()
        .content("기존 내용")
        .build();

    // when
    comment.updateContent("수정된 내용");

    // then
    assertThat(comment.getContent()).isEqualTo("수정된 내용");
  }
}