package com.jsp.book.entity;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class BookedTicket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String movieName;
	private String screenName;
	private String theaterName;

	@ElementCollection
	private String[] seatNumber;

	private String qrUrl;
	private String orderId;
	private String paymentId;

	private String showDate;
	private String showTiming;

	private Double ticketPrice;
	private Integer ticketCount;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	private Long showId;
}
