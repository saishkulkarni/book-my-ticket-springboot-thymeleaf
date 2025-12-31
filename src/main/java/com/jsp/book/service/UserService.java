package com.jsp.book.service;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
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
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

public interface UserService {

	/* ---------- Common ---------- */

	String loadMain(ModelMap map);

	String login(LoginDto dto, RedirectAttributes attributes, HttpSession session);

	String logout(HttpSession session, RedirectAttributes attributes);

	/* ---------- Registration & OTP ---------- */

	String register(UserDto userDto, BindingResult result, RedirectAttributes attributes);

	String submitOtp(int otp, String email, RedirectAttributes attributes);

	String resendOtp(String email, RedirectAttributes attributes);

	/* ---------- Password ---------- */

	String forgotPassword(String email, RedirectAttributes attributes);

	String resetPassword(PasswordDto passwordDto, BindingResult result, RedirectAttributes attributes, ModelMap map);

	/* ---------- Users (Admin) ---------- */

	String manageUsers(HttpSession session, RedirectAttributes attributes, ModelMap map);

	String blockUser(Long id, HttpSession session, RedirectAttributes attributes);

	String unBlockUser(Long id, HttpSession session, RedirectAttributes attributes);

	/* ---------- Theater ---------- */

	String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session);

	String loadAddTheater(HttpSession session, RedirectAttributes attributes, TheaterDto theaterDto);

	String addTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result) throws IOException;

	String editTheater(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String updateTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result, Long id);

	String deleteTheater(Long id, HttpSession session, RedirectAttributes attributes);

	/* ---------- Screen ---------- */

	String manageScreens(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String addScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map, ScreenDto screenDto);

	String addScreen(ScreenDto screenDto, BindingResult result, HttpSession session, RedirectAttributes attributes);

	String editScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String updateScreen(ScreenDto screenDto, Long id, BindingResult result, HttpSession session,
			RedirectAttributes attributes, ModelMap map);

	String deleteScreen(Long id, HttpSession session, RedirectAttributes attributes);

	/* ---------- Seats ---------- */

	String manageSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes);

	String addSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes);

	String saveSeats(Long id, SeatLayoutForm seatLayoutForm, HttpSession session, RedirectAttributes attributes);

	/* ---------- Movie ---------- */

	String manageMovies(HttpSession session, RedirectAttributes attributes, ModelMap map);

	String loadAddMovie(MovieDto movieDto, RedirectAttributes attributes, HttpSession session);

	String addMovie(MovieDto movieDto, BindingResult result, RedirectAttributes attributes, HttpSession session);

	String deleteMovie(Long id, HttpSession session, RedirectAttributes attributes);

	/* ---------- Show & Booking ---------- */

	String manageShows(Long id, ModelMap map, RedirectAttributes attributes, HttpSession session);

	String addShow(Long id, ModelMap map, RedirectAttributes attributes, HttpSession session);

	String addShow(ShowDto showDto, BindingResult result, RedirectAttributes attributes, HttpSession session,
			ModelMap map);

	String displayShowsOnDate(LocalDate date, Long movieId, RedirectAttributes attributes, ModelMap map);

	String bookMovie(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String showSeats(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map);

	String deleteShow(Long id, HttpSession session, RedirectAttributes attributes);

	String confirmBooking(Long showId, Long[] seatIds, HttpSession session, ModelMap map, RedirectAttributes attributes)
			throws RazorpayException;

	String confirmTicket(HttpSession session, ModelMap map, RedirectAttributes attributes, String razorpay_order_id,
			String razorpay_payment_id) throws IOException, WriterException;
}
