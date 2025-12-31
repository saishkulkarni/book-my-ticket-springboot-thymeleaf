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

	@Size(min = 3, max = 50, message = "* Enter between 3 ~ 50 characters")
	private String name;

	@Size(min = 3, max = 200, message = "* Enter between 3 ~ 200 characters")
	private String address;

	@NotBlank(message = "* Location Link is Required")
	private String locationLink;

	@NotNull(message = "* Image is Required")
	private MultipartFile image;
}
