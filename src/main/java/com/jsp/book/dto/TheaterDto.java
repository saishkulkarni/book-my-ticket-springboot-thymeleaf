package com.jsp.book.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TheaterDto {
	@Size(min = 3, max = 50, message = "* Enter between 3~50 charecters")
	private String name;
	@Size(min = 3, max = 200, message = "* Enter between 3~200 charecters")
	private String address;
	@NotBlank(message = "* It is Required")
	private String locationLink;
	@NotNull(message = "* It is Required")
	private MultipartFile image;
}