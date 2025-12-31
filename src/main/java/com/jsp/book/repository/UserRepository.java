package com.jsp.book.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	boolean existsByEmail(String email);
	boolean existsByMobile(Long mobile);
	Optional<User> findByEmail(String email);
	List<User> findByRole(String role);
	void deleteByRole(String role);
}

