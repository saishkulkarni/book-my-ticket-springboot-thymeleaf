package com.jsp.book.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PasswordDto {
	@DecimalMin(value = "100000", message = "* Invalid OTP")
	@DecimalMax(value = "999999", message = "* Invalid OTP")
	private int otp;
	@Pattern(regexp = "^.*(?=.{8,})(?=..*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$", message = "* Select a Stronger Password")
	private String password;
	private String email;
}
