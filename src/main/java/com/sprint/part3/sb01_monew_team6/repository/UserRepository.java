package com.sprint.part3.sb01_monew_team6.repository;

import com.sprint.part3.sb01_monew_team6.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
