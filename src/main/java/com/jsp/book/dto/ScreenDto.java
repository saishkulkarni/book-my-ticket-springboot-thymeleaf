package com.jsp.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScreenDto {

	@NotBlank(message = "* Screen Name is Required")
	private String name;

	@NotBlank(message = "* Screen Type is Required")
	private String type;

	@NotNull(message = "* Theater is Required")
	private Long theaterId;
}
