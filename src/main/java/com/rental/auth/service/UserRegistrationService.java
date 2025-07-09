package com.rental.auth.service;

import com.rental.auth.DTO.RegisterRequest;
import com.rental.common.exception.BusinessException;
import com.rental.permission.model.Role;
import com.rental.permission.repository.RoleRepository;
import com.rental.user.model.User;
import com.rental.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户注册服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @Transactional
    public void register(@Valid RegisterRequest request) {
        // 验证用户输入
        validateRegistrationRequest(request);

        // 检查用户名是否已存在
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(User.UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 分配默认角色（客户角色）
        Role customerRole = roleRepository.findByName("CUSTOMER")
            .orElseThrow(() -> new BusinessException("默认角色配置错误"));

        user.setRoles(Set.of(customerRole));

        // 保存用户
        userRepository.save(user);

        log.info("用户 {} 注册成功", request.getUsername());
    }

    /**
     * 验证注册请求
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        // 验证密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 验证密码强度（可以根据需求调整）
        if (request.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }

        // 可以添加更多验证规则
        // 例如：密码复杂度、用户名格式等
    }
}
