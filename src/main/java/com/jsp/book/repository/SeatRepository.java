package com.jsp.book.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {
	List<Seat> findByScreenOrderBySeatRowAscSeatColumnAsc(Screen screen);
	Optional<Seat> findBySeatNumber(String seatNumber);
}

