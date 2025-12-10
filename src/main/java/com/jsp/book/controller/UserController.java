package com.jsp.book.controller;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.MovieDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.ScreenDto;
import com.jsp.book.dto.TheaterDto;
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
	public String resetPassword(@Valid PasswordDto passwordDto, BindingResult result, ModelMap map,
			RedirectAttributes attributes) {
		return userService.resetPassword(passwordDto, result, attributes, map);
	}

	@GetMapping("/manage-users")
	public String viewUsers(HttpSession session, RedirectAttributes attributes, ModelMap map) {
		return userService.manageUsers(session, attributes, map);
	}

	@GetMapping("/block/{id}")
	public String block(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.blockUser(id, session, attributes);
	}

	@GetMapping("/un-block/{id}")
	public String unBlock(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.unBlockUser(id, session, attributes);
	}

	@GetMapping("/manage-theaters")
	public String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session) {
		return userService.manageTheater(map, attributes, session);
	}

	@GetMapping("/add-theater")
	public String addTheater(TheaterDto theaterDto, RedirectAttributes attributes, HttpSession session) {
		return userService.loadAddTheater(session, attributes, theaterDto);
	}

	@PostMapping("/add-theater")
	public String addTheater(@Valid TheaterDto theaterDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session) throws IOException {
		return userService.addTheater(session, attributes, theaterDto, result);
	}

	@GetMapping("/delete-theater/{id}")
	public String deleteTheater(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.deleteTheater(id, session, attributes);
	}

	@GetMapping("/edit-theater/{id}")
	public String editTheater(@PathVariable Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		return userService.editTheater(id, session, attributes, map);
	}

	@PostMapping("/update-theater")
	public String updateTheater(@Valid TheaterDto theaterDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session, @RequestParam("id") Long id) throws IOException {
		return userService.updateTheater(session, attributes, theaterDto, result, id);
	}

	@GetMapping("/manage-screens/{id}")
	public String manageScreens(@PathVariable Long id, HttpSession session, RedirectAttributes attributes,
			ModelMap map) {
		return userService.manageScreens(id, session, attributes, map);
	}

	@GetMapping("/add-screen/{id}")
	public String addScreen(@PathVariable Long id, HttpSession session, RedirectAttributes attributes, ModelMap map,
			ScreenDto screenDto) {
		return userService.addScreen(id, session, attributes, map, screenDto);
	}

	@PostMapping("/add-screen")
	public String addScreen(@Valid ScreenDto screenDto, BindingResult result, HttpSession session,
			RedirectAttributes attributes) {
		return userService.addScreen(screenDto, result, session, attributes);
	}

	@GetMapping("/delete-screen/{id}")
	public String deleteScreen(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.deleteScreen(id, session, attributes);
	}

	@GetMapping("/edit-screen/{id}")
	public String edditScreen(@PathVariable Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		return userService.editScreen(id, session, attributes, map);
	}

	@PostMapping("/update-screen")
	public String updateScreen(@Valid ScreenDto screenDto, BindingResult result, @RequestParam Long id, ModelMap map,
			RedirectAttributes attributes, HttpSession session) {
		return userService.updateScreen(screenDto, id, result, session, attributes, map);
	}

	@GetMapping("/manage-seats/{id}")
	public String manageSeats(@PathVariable Long id, HttpSession session, ModelMap map, RedirectAttributes attributes) {
		return userService.manageSeats(id, session, map, attributes);
	}

	@GetMapping("/add-seats/{id}")
	public String addSeats(@PathVariable Long id, HttpSession session, ModelMap map, RedirectAttributes attributes) {
		return userService.addSeats(id, session, map, attributes);
	}

	@GetMapping("/manage-movies")
	public String manageMovies(HttpSession session, RedirectAttributes attributes, ModelMap map) {
		return userService.manageMovies(session, attributes, map);
	}

	@GetMapping("/add-movie")
	public String loadAddMovie(MovieDto movieDto, RedirectAttributes attributes, HttpSession session) {
		return userService.loadAddMovie(movieDto, attributes, session);
	}

	@PostMapping("/add-movie")
	public String addMovie(@Valid MovieDto movieDto,BindingResult result ,RedirectAttributes attributes, HttpSession session) {
		return userService.addMovie(movieDto,result, attributes, session);
	}
}
