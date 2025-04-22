package com.sprint.part3.sb01_monew_team6.service;

import com.sprint.part3.sb01_monew_team6.dto.UserLoginRequest;
import com.sprint.part3.sb01_monew_team6.dto.UserRegisterRequest;
import com.sprint.part3.sb01_monew_team6.entity.User;

public interface UserService {
    User registerUser(UserRegisterRequest request);

    User login(UserLoginRequest request);

    void updateNickname(Long userId, String newNickname);

    void deleteUser(Long userId);
}