package com.jsp.book.service;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.UserDto;

import jakarta.servlet.http.HttpSession;

public interface UserService {
	String register(UserDto userDto, BindingResult result);

	String login(LoginDto dto, RedirectAttributes attributes,HttpSession session);
}
