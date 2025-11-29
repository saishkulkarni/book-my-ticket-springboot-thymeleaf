package com.jsp.book.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping({ "/", "/main" })
	public String loadMain() {
		return "main.html";
	}

	@GetMapping("/register")
	public String loadRegister(UserDto userDto) {
		return "register.html";
	}

	@PostMapping("/register")
	public String register(@Valid UserDto userDto, BindingResult result, RedirectAttributes attributes) {
		return userService.register(userDto, result, attributes);
	}

	@GetMapping("/login")
	public String loadLogin() {
		return "login.html";
	}

	@PostMapping("/login")
	public String login(LoginDto dto, RedirectAttributes attributes, HttpSession session) {
		return userService.login(dto, attributes, session);
	}

	@GetMapping("/logout")
	public String logout(HttpSession session, RedirectAttributes attributes) {
		return userService.logout(session, attributes);
	}

	@GetMapping("/otp")
	public String loadOtpPage() {
		return "otp.html";
	}

	@PostMapping("/otp")
	public String submitOtp(@RequestParam int otp, @RequestParam String email, RedirectAttributes attributes) {
		return userService.submitOtp(otp, email, attributes);
	}

	@GetMapping("/resend-otp/{email}")
	public String resendOtp(@PathVariable String email, RedirectAttributes attributes) {
		return userService.resendOtp(email, attributes);
	}

	@GetMapping("/forgot-password")
	public String forgotPassword() {
		return "forgot-password.html";
	}

	@PostMapping("/forgot-password")
	public String forgotPassword(@RequestParam String email, RedirectAttributes attributes) {
		return userService.forgotPassword(email, attributes);
	}

	@GetMapping("/reset-password")
	public String resetPassword(PasswordDto passwordDto) {
		return "reset-password.html";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@Valid PasswordDto passwordDto,BindingResult result,ModelMap map, RedirectAttributes attributes) {
		return userService.resetPassword(passwordDto,result, attributes,map);
	}
}
