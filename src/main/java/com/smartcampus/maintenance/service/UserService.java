package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.dto.PasswordChangeRequest;
import com.smartcampus.maintenance.dto.ProfileUpdateRequest;
import com.smartcampus.maintenance.dto.RegistrationRequest;
import com.smartcampus.maintenance.dto.StaffCreationRequest;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    User registerStudent(RegistrationRequest request);
    User createStaffMember(StaffCreationRequest request);
    User findByEmail(String email);
    User findById(Long id);
    List<User> getAllActiveStaffMembers();
    Page<User> getStudents(String search, Pageable pageable);
    Page<User> getStaffMembers(Pageable pageable);
    User toggleUserStatus(Long userId);
    User updateProfile(User user, ProfileUpdateRequest request);
    void changePassword(User user, PasswordChangeRequest request);
}
