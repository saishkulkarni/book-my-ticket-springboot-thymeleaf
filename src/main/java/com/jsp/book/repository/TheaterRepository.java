package com.jsp.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Theater;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

	boolean existsByNameAndAddress(String name, String address);

}
