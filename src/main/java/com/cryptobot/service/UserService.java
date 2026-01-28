package com.cryptobot.service;

import com.cryptobot.domain.model.User;
import com.cryptobot.repository.UserRepository;
import com.cryptobot.repository.entity.UserEntity;
import com.cryptobot.service.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public User createUser(String email, String username) {
        UserEntity entity = UserEntity.builder()
                .email(email)
                .username(username)
                .build();
        return userMapper.toDomain(userRepository.save(entity));
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDomain);
    }
}
