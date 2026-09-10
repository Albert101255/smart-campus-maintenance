package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRegisterNumber(String registerNumber);
    boolean existsByEmail(String email);
    boolean existsByRegisterNumber(String registerNumber);
    List<User> findByRoleAndEnabledTrue(Role role);
    Page<User> findByRole(Role role, Pageable pageable);
    Page<User> findByRoleAndNameContainingIgnoreCaseOrEmailContainingIgnoreCase(Role role, String name, String email, Pageable pageable);
    long countByRole(Role role);
}
