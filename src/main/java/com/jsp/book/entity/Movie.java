package com.jsp.book.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String languages;
	@Column(nullable = false)
	private String genre;
	@Column(nullable = false)
	private String duration;
	@Column(nullable = false)
	private String imageLink;
	@Column(nullable = false)
	private String trailerLink;
	@Column(nullable = false, length = 500)
	private String description;
	@Column(nullable = false)
	private LocalDate releaseDate;
	@Column(nullable = false)
	private String cast;
}