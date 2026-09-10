package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.dto.PasswordChangeRequest;
import com.smartcampus.maintenance.dto.ProfileUpdateRequest;
import com.smartcampus.maintenance.dto.RegistrationRequest;
import com.smartcampus.maintenance.dto.StaffCreationRequest;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.Role;
import com.smartcampus.maintenance.exception.UserNotFoundException;
import com.smartcampus.maintenance.repository.UserRepository;
import com.smartcampus.maintenance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerStudent(RegistrationRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address is already registered");
        }
        if (userRepository.existsByRegisterNumber(request.getRegisterNumber())) {
            throw new IllegalArgumentException("Register/Roll number is already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRegisterNumber(request.getRegisterNumber().trim());
        user.setDepartment(request.getDepartment());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Override
    public User createStaffMember(StaffCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email address is already registered");
        }

        User staff = new User();
        staff.setName(request.getName());
        staff.setEmail(request.getEmail().toLowerCase().trim());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setDepartment(request.getDepartment());
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setRole(Role.MAINTENANCE_STAFF);
        staff.setEnabled(true);

        return userRepository.save(staff);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllActiveStaffMembers() {
        return userRepository.findByRoleAndEnabledTrue(Role.MAINTENANCE_STAFF);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getStudents(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.findByRoleAndNameContainingIgnoreCaseOrEmailContainingIgnoreCase(Role.STUDENT, search.trim(), search.trim(), pageable);
        }
        return userRepository.findByRole(Role.STUDENT, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getStaffMembers(Pageable pageable) {
        return userRepository.findByRole(Role.MAINTENANCE_STAFF, pageable);
    }

    @Override
    public User toggleUserStatus(Long userId) {
        User user = findById(userId);
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Override
    public User updateProfile(User user, ProfileUpdateRequest request) {
        User existingUser = findById(user.getId());
        existingUser.setName(request.getName());
        existingUser.setPhoneNumber(request.getPhoneNumber());
        if (request.getDepartment() != null) {
            existingUser.setDepartment(request.getDepartment());
        }
        return userRepository.save(existingUser);
    }

    @Override
    public void changePassword(User user, PasswordChangeRequest request) {
        User existingUser = findById(user.getId());
        if (!passwordEncoder.matches(request.getCurrentPassword(), existingUser.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }
        existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(existingUser);
    }
}
