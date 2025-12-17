package com.jsp.book.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieDto {
	@NotBlank(message = "* It is Required")
	private String name;
	@NotBlank(message = "* It is Required")
	private String languages;
	@NotBlank(message = "* It is Required")
	private String genre;
	@NotNull(message = "* It is Required")
	private LocalTime duration;
	private MultipartFile image;
	@NotBlank(message = "* It is Required")
	private String trailerLink;
	@NotBlank(message = "* It is Required")
	private String description;
	@NotNull(message = "* It is Required")
	private LocalDate releaseDate;
	@NotBlank(message = "* It is Required")
	private String cast;
}
