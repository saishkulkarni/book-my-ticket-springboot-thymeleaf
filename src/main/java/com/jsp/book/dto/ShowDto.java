package com.jsp.book.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShowDto {

	@NotNull(message = "* Show Date is Required")
	private LocalDate showDate;

	@NotNull(message = "* Start Time is Required")
	private LocalTime startTime;

	@NotNull(message = "* Movie is Required")
	private Long movieId;

	@NotNull(message = "* Ticket Price is Required")
	private Double ticketPrice;

	@NotNull(message = "* Screen is Required")
	private Long screenId;
}
