package com.jsp.book.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {

	@NotBlank(message = "* Email is Required")
	@Email(message = "* Enter Proper Email")
	private String email;

	@NotBlank(message = "* Password is Required")
	private String password;
}
