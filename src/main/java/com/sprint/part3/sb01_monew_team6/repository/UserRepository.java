package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<엔티티 클래스, 엔티티의 ID 타입>
public interface UserRepository extends JpaRepository<User, Long> {

        // 이메일로 사용자를 찾는 쿼리 메소드 정의
     Optional<User> findByEmail(String email);

  // 이메일 존재 여부를 확인하는 쿼리 메소드 정의
     boolean existsByEmail(String email);

    Optional<User> findByIdAndIsDeletedFalse(Long id);

    boolean existsByIdAndIsDeletedFalse(Long id);
}