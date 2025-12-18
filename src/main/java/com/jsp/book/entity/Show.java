package com.jsp.book.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "movieshow")
public class Show {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private LocalDate showDate;
	private LocalTime startTime;
	private LocalTime endTime;
	@ManyToOne
	private Movie movie;
	private Double ticketPrice;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<ShowSeat> seats;
	@ManyToOne
	private Screen screen;
}
