package com.jsp.book.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.zxing.WriterException;
import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.MovieDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.ScreenDto;
import com.jsp.book.dto.SeatLayoutForm;
import com.jsp.book.dto.ShowDto;
import com.jsp.book.dto.TheaterDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.service.UserService;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	/* ---------- Common ---------- */

	@GetMapping({ "/", "/main" })
	public String loadMain(ModelMap model) {
		return userService.loadMain(model);
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

	/* ---------- Registration & OTP ---------- */

	@GetMapping("/register")
	public String loadRegister(UserDto userDto) {
		return "register.html";
	}

	@PostMapping("/register")
	public String register(@Valid UserDto userDto, BindingResult result, RedirectAttributes attributes) {
		return userService.register(userDto, result, attributes);
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

	/* ---------- Password ---------- */

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
	public String resetPassword(@Valid PasswordDto passwordDto, BindingResult result, ModelMap model,
			RedirectAttributes attributes) {
		return userService.resetPassword(passwordDto, result, attributes, model);
	}

	/* ---------- Users (Admin) ---------- */

	@GetMapping("/manage-users")
	public String manageUsers(HttpSession session, RedirectAttributes attributes, ModelMap model) {
		return userService.manageUsers(session, attributes, model);
	}

	@GetMapping("/block/{id}")
	public String blockUser(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.blockUser(id, session, attributes);
	}

	@GetMapping("/un-block/{id}")
	public String unblockUser(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.unBlockUser(id, session, attributes);
	}

	/* ---------- Theater ---------- */

	@GetMapping("/manage-theaters")
	public String manageTheater(ModelMap model, RedirectAttributes attributes, HttpSession session) {
		return userService.manageTheater(model, attributes, session);
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
	public String editTheater(@PathVariable Long id, HttpSession session, RedirectAttributes attributes,
			ModelMap model) {
		return userService.editTheater(id, session, attributes, model);
	}

	@PostMapping("/update-theater")
	public String updateTheater(@Valid TheaterDto theaterDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session, @RequestParam Long id) throws IOException {
		return userService.updateTheater(session, attributes, theaterDto, result, id);
	}

	/* ---------- Screen ---------- */

	@GetMapping("/manage-screens/{id}")
	public String manageScreens(@PathVariable Long id, HttpSession session, RedirectAttributes attributes,
			ModelMap model) {
		return userService.manageScreens(id, session, attributes, model);
	}

	@GetMapping("/add-screen/{id}")
	public String addScreen(@PathVariable Long id, HttpSession session, RedirectAttributes attributes, ModelMap model,
			ScreenDto screenDto) {
		return userService.addScreen(id, session, attributes, model, screenDto);
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
	public String editScreen(@PathVariable Long id, HttpSession session, RedirectAttributes attributes,
			ModelMap model) {
		return userService.editScreen(id, session, attributes, model);
	}

	@PostMapping("/update-screen")
	public String updateScreen(@Valid ScreenDto screenDto, BindingResult result, @RequestParam Long id, ModelMap model,
			RedirectAttributes attributes, HttpSession session) {
		return userService.updateScreen(screenDto, id, result, session, attributes, model);
	}

	/* ---------- Seats ---------- */

	@GetMapping("/manage-seats/{id}")
	public String manageSeats(@PathVariable Long id, HttpSession session, ModelMap model,
			RedirectAttributes attributes) {
		return userService.manageSeats(id, session, model, attributes);
	}

	@GetMapping("/add-seats/{id}")
	public String addSeats(@PathVariable Long id, HttpSession session, ModelMap model, RedirectAttributes attributes) {
		return userService.addSeats(id, session, model, attributes);
	}

	@PostMapping("/add-seats/{id}")
	public String saveSeats(@PathVariable Long id, SeatLayoutForm seatLayoutForm, HttpSession session,
			RedirectAttributes attributes) {
		return userService.saveSeats(id, seatLayoutForm, session, attributes);
	}

	/* ---------- Movie ---------- */

	@GetMapping("/manage-movies")
	public String manageMovies(HttpSession session, RedirectAttributes attributes, ModelMap model) {
		return userService.manageMovies(session, attributes, model);
	}

	@GetMapping("/add-movie")
	public String loadAddMovie(MovieDto movieDto, RedirectAttributes attributes, HttpSession session) {
		return userService.loadAddMovie(movieDto, attributes, session);
	}

	@PostMapping("/add-movie")
	public String addMovie(@Valid MovieDto movieDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session) {
		return userService.addMovie(movieDto, result, attributes, session);
	}

	@GetMapping("/delete-movie/{id}")
	public String deleteMovie(@PathVariable Long id, HttpSession session, RedirectAttributes attributes) {
		return userService.deleteMovie(id, session, attributes);
	}

	/* ---------- Show & Booking ---------- */

	@GetMapping("/manage-shows/{id}")
	public String manageShows(@PathVariable Long id, ModelMap model, RedirectAttributes attributes,
			HttpSession session) {
		return userService.manageShows(id, model, attributes, session);
	}

	@GetMapping("/add-show/{id}")
	public String addShow(@PathVariable Long id, ModelMap model, RedirectAttributes attributes, HttpSession session) {
		return userService.addShow(id, model, attributes, session);
	}

	@PostMapping("/add-show")
	public String addShow(@Valid ShowDto showDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session, ModelMap model) {
		return userService.addShow(showDto, result, attributes, session, model);
	}

	@GetMapping("/book/movie/{id}")
	public String bookMovie(@PathVariable Long id, HttpSession session, RedirectAttributes attributes, ModelMap model) {
		return userService.bookMovie(id, session, attributes, model);
	}

	@GetMapping("/delete-show/{id}")
	public String deleteShow(@PathVariable Long id, RedirectAttributes attributes, HttpSession session) {
		return userService.deleteShow(id, session, attributes);
	}

	@GetMapping("/selectShows")
	public String displayShows(@RequestParam Long movieId, @RequestParam LocalDate date, RedirectAttributes attributes,
			ModelMap model) {
		return userService.displayShowsOnDate(date, movieId, attributes, model);
	}

	@GetMapping("/show-seats/{id}")
	public String showSeats(@PathVariable Long id, HttpSession session, RedirectAttributes attributes, ModelMap model) {
		return userService.showSeats(id, session, attributes, model);
	}

	@PostMapping("/confirm-booking")
	public String confirmBooking(@RequestParam Long showId, @RequestParam Long[] seatIds, HttpSession session,
			ModelMap model, RedirectAttributes attributes) throws RazorpayException {
		return userService.confirmBooking(showId, seatIds, session, model, attributes);
	}

	@PostMapping("/confirm-ticket")
	public String confirmTicket(HttpSession session, ModelMap model, RedirectAttributes attributes,
			@RequestParam String razorpay_payment_id, @RequestParam String razorpay_order_id)
			throws IOException, WriterException {
		return userService.confirmTicket(session, model, attributes, razorpay_order_id, razorpay_payment_id);
	}
}
