package com.jsp.book.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.book.entity.BookedTicket;

public interface TicketRepository extends JpaRepository<BookedTicket, Long> {

}
