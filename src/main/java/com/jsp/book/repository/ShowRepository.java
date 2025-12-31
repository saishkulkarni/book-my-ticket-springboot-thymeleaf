package com.jsp.book.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Movie;
import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Show;

public interface ShowRepository extends JpaRepository<Show, Long> {
	List<Show> findByScreen(Screen screen);
	List<Show> findByShowDateAfter(LocalDate date);
	List<Show> findByMovieAndShowDateAfter(Movie movie, LocalDate date);
	boolean existsByScreen(Screen screen);
	boolean existsByMovie(Movie movie);
	List<Show> findByShowDateAndMovie(LocalDate showDate, Movie movie);
}
