package com.jsp.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Seat;
import com.jsp.book.entity.ShowSeat;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
	List<ShowSeat> findBySeatIn(List<Seat> seats);
}
