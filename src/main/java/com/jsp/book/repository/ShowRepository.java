package com.jsp.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Show;

public interface ShowRepository extends JpaRepository<Show, Long> {

	List<Show> findByScreen(Screen screen);

}
