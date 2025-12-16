package com.jsp.book.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Seat;

public interface SeatRepository extends JpaRepository<Seat, Long> {

	List<Seat> findByScreenOrderBySeatRowAscSeatColumnAsc(Screen screen);

}
