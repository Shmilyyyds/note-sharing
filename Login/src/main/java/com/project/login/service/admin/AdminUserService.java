package com.project.login.service.admin;

import com.project.login.model.request.admin.UserAdminUpdateRequest;
import com.project.login.model.entity.UserEntity;
import com.project.login.repository.NoteSpaceRepository;
import com.project.login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final NoteSpaceRepository noteSpaceRepository;

    @Transactional
    public UserEntity updateUser(Long id, UserAdminUpdateRequest req) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));

        if (req.getUsername() != null && !Objects.equals(user.getUsername(), req.getUsername())) {
            user.setUsername(req.getUsername());
        }

        if (req.getStudentNumber() != null && !Objects.equals(user.getStudentNumber(), req.getStudentNumber())) {
            if (userRepository.existsByStudentNumber(req.getStudentNumber())) {
                throw new RuntimeException("学号已存在");
            }
            user.setStudentNumber(req.getStudentNumber());
        }

        if (req.getEmail() != null && !Objects.equals(user.getEmail(), req.getEmail())) {
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new RuntimeException("邮箱已存在");
            }
            user.setEmail(req.getEmail());
        }

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("用户不存在"));
        noteSpaceRepository.findEntitiesByUserId(id).forEach(space -> noteSpaceRepository.deleteById(space.getId()));
        userRepository.delete(user);
    }
}
