package com.smartinventory.user.service;

import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.user.dto.UpdateUserRequest;
import com.smartinventory.user.dto.UserResponse;
import com.smartinventory.user.entity.Role;
import com.smartinventory.user.entity.User;
import com.smartinventory.user.enums.RoleName;
import com.smartinventory.user.repository.RoleRepository;
import com.smartinventory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Kullanıcı bulunamadı: " + email));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public void deactivateUser(Long id) {
        if (id.equals(currentUserId())) {
            throw new IllegalArgumentException("Kendi hesabınızı pasife alamazsınız");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta zaten kullanılıyor: " + request.getEmail());
        }

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setDepartment(request.getDepartment());
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateRole(Long id, String roleName) {
        if (id.equals(currentUserId())) {
            throw new IllegalArgumentException("Kendi rolünüzü değiştiremezsiniz");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        RoleName parsed;
        try {
            parsed = RoleName.valueOf(roleName);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Geçersiz rol adı: " + roleName);
        }
        Role role = roleRepository.findByName(parsed)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", parsed));

        user.setRole(role);
        return mapToResponse(userRepository.save(user));
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User principal) {
            return principal.getId();
        }
        return null;
    }

    public UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .role(user.getRole().getName().name())
                .active(user.isActive())
                .build();
    }
}
