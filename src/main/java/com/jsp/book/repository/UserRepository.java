package com.jsp.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
