package com.jsp.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Theater;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

	List<Screen> findByTheater(Theater theater);

	boolean existsByNameAndTheater(String name, Theater theater);

}
