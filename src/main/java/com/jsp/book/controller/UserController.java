package com.jsp.book.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping({"/","/main"})
	public String loadMain() {
		return "main.html";
	}

	@GetMapping("/register")
	public String loadRegister(UserDto userDto) {
		return "register.html";
	}

	@PostMapping("/register")
	public String register(@Valid UserDto userDto, BindingResult result) {
		return userService.register(userDto, result);
	}

	@GetMapping("/login")
	public String loadLogin() {
		return "login.html";
	}
	
	@PostMapping("/login")
	public String login(LoginDto dto,RedirectAttributes attributes,HttpSession session) {
		return userService.login(dto,attributes,session);
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session,RedirectAttributes attributes) {
		return userService.logout(session,attributes);
	}
}
