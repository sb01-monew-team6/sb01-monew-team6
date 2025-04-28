package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {

}
