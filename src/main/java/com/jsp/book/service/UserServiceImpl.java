package com.jsp.book.service;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.zxing.WriterException;
import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.MovieDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.ScreenDto;
import com.jsp.book.dto.SeatLayoutForm;
import com.jsp.book.dto.SeatRowDto;
import com.jsp.book.dto.ShowDto;
import com.jsp.book.dto.TheaterDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.BookedTicket;
import com.jsp.book.entity.Movie;
import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Seat;
import com.jsp.book.entity.Show;
import com.jsp.book.entity.ShowSeat;
import com.jsp.book.entity.Theater;
import com.jsp.book.entity.User;
import com.jsp.book.repository.MovieRepository;
import com.jsp.book.repository.ScreenRepository;
import com.jsp.book.repository.SeatRepository;
import com.jsp.book.repository.ShowRepository;
import com.jsp.book.repository.ShowSeatRepository;
import com.jsp.book.repository.TheaterRepository;
import com.jsp.book.repository.TicketRepository;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.AES;
import com.jsp.book.util.CloudinaryHelper;
import com.jsp.book.util.EmailHelper;
import com.jsp.book.util.QrHelper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	/* ---------- Repositories ---------- */
	private final UserRepository userRepository;
	private final TheaterRepository theaterRepository;
	private final ScreenRepository screenRepository;
	private final MovieRepository movieRepository;
	private final ShowRepository showRepository;
	private final SeatRepository seatRepository;
	private final ShowSeatRepository showSeatRepository;
	private final TicketRepository ticketRepository;

	/* ---------- Helpers & Utilities ---------- */
	private final SecureRandom secureRandom;
	private final EmailHelper emailHelper;
	private final RedisService redisService;
	private final CloudinaryHelper cloudinaryHelper;
	private final QrHelper qrHelper;

	/* ---------- Razorpay Credentials ---------- */
	private static final String RAZORPAY_KEY = "rzp_test_RaPsJq0rZSFWD1";
	private static final String RAZORPAY_SECRET = "ZO0swacFGMgyE71JVvGuBOFP";

	@Override
	public String register(UserDto userDto, BindingResult result, RedirectAttributes attributes) {

		// Password match validation
		if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
			result.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and Confirm Password should be same");
		}

		// Uniqueness checks
		if (userRepository.existsByEmail(userDto.getEmail())) {
			result.rejectValue("email", "error.email", "* Email should be unique");
		}

		if (userRepository.existsByMobile(userDto.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Mobile number should be unique");
		}

		// Validation failure
		if (result.hasErrors()) {
			return "register.html";
		}

		// OTP generation & persistence
		int otp = secureRandom.nextInt(100000, 1_000_000);

		emailHelper.sendOtp(otp, userDto.getName(), userDto.getEmail());
		redisService.saveUserDto(userDto.getEmail(), userDto);
		redisService.saveOtp(userDto.getEmail(), otp);

		attributes.addFlashAttribute("pass", "Otp Sent Success");
		attributes.addFlashAttribute("email", userDto.getEmail());

		return "redirect:/otp";
	}

	@Override
	public String login(LoginDto dto, RedirectAttributes attributes, HttpSession session) {

		Optional<User> optionalUser = userRepository.findByEmail(dto.getEmail());

		// Email validation
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/login";
		}

		User user = optionalUser.get();

		// Password validation
		String decryptedPassword = AES.decrypt(user.getPassword());
		if (!decryptedPassword.equals(dto.getPassword())) {
			attributes.addFlashAttribute("fail", "Invalid Password");
			return "redirect:/login";
		}

		// Blocked user check
		if (user.isBlocked()) {
			attributes.addFlashAttribute("fail", "Account Blocked!, Contact Admin");
			return "redirect:/login";
		}

		// Successful login
		session.setAttribute("user", user);
		attributes.addFlashAttribute("pass", "Login Success");

		return "redirect:/main";
	}

	@Override
	public String logout(HttpSession session, RedirectAttributes attributes) {
		session.invalidate();
		attributes.addFlashAttribute("pass", "Logout Success");
		return "redirect:/main";
	}

	@Override
	public String submitOtp(int otp, String email, RedirectAttributes attributes) {

		UserDto userDto = redisService.getUserDto(email);

		// User DTO expired (timeout)
		if (userDto == null) {
			attributes.addFlashAttribute("fail", "Timeout Try Again Creating a New Account");
			return "redirect:/register";
		}

		int storedOtp = redisService.getOtp(email);

		// OTP expired
		if (storedOtp == 0) {
			attributes.addFlashAttribute("fail", "OTP Expired, Resend Otp and Try Again");
			attributes.addFlashAttribute("email", email);
			return "redirect:/otp";
		}

		// OTP mismatch
		if (otp != storedOtp) {
			attributes.addFlashAttribute("fail", "Invalid OTP Try Again");
			attributes.addFlashAttribute("email", email);
			return "redirect:/otp";
		}

		// OTP success → create user
		User user = new User(null, userDto.getName(), userDto.getEmail(), userDto.getMobile(),
				AES.encrypt(userDto.getPassword()), "USER", false);

		userRepository.save(user);

		attributes.addFlashAttribute("pass", "Account Registered Success");
		return "redirect:/main";
	}

	@Override
	public String resendOtp(String email, RedirectAttributes attributes) {

		UserDto userDto = redisService.getUserDto(email);

		// DTO expired
		if (userDto == null) {
			attributes.addFlashAttribute("fail", "Timeout Try Again Creating a New Account");
			return "redirect:/register";
		}

		int otp = secureRandom.nextInt(100000, 1_000_000);

		emailHelper.sendOtp(otp, userDto.getName(), userDto.getEmail());
		redisService.saveOtp(userDto.getEmail(), otp);

		attributes.addFlashAttribute("pass", "Otp Re-Sent Success");
		attributes.addFlashAttribute("email", userDto.getEmail());

		return "redirect:/otp";
	}

	@Override
	public String forgotPassword(String email, RedirectAttributes attributes) {

		Optional<User> optionalUser = userRepository.findByEmail(email);

		// Email validation
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/forgot-password";
		}

		User user = optionalUser.get();

		int otp = secureRandom.nextInt(100000, 1_000_000);

		emailHelper.sendOtp(otp, user.getName(), email);
		redisService.saveOtp(email, otp);

		attributes.addFlashAttribute("pass", "Sent Success");
		attributes.addFlashAttribute("email", email);

		return "redirect:/reset-password";
	}

	@Override
	public String resetPassword(PasswordDto passwordDto, BindingResult result, RedirectAttributes attributes,
			ModelMap map) {

		// Validation errors (form-level)
		if (result.hasErrors()) {
			map.put("email", passwordDto.getEmail());
			return "reset-password.html";
		}

		Optional<User> optionalUser = userRepository.findByEmail(passwordDto.getEmail());

		// Invalid email
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/forgot-password";
		}

		int storedOtp = redisService.getOtp(passwordDto.getEmail());

		// OTP expired
		if (storedOtp == 0) {
			attributes.addFlashAttribute("fail", "OTP Expired, Resend Otp and Try Again");
			attributes.addFlashAttribute("email", passwordDto.getEmail());
			return "redirect:/reset-password";
		}

		// OTP mismatch
		if (passwordDto.getOtp() != storedOtp) {
			attributes.addFlashAttribute("fail", "Invalid OTP Try Again");
			attributes.addFlashAttribute("email", passwordDto.getEmail());
			return "redirect:/reset-password";
		}

		// OTP success → update password
		User user = optionalUser.get();
		user.setPassword(AES.encrypt(passwordDto.getPassword()));
		userRepository.save(user);

		attributes.addFlashAttribute("pass", "Password Reset Success");
		return "redirect:/main";
	}

	@Override
	public String manageUsers(HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		List<User> users = userRepository.findByRole("USER");

		// No users found
		if (users.isEmpty()) {
			attributes.addFlashAttribute("fail", "No Users Registered Yet");
			return "redirect:/";
		}

		map.put("users", users);
		return "manage-users.html";
	}

	@Override
	public String blockUser(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<User> optionalUser = userRepository.findById(id);

		// Target user not found
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		User targetUser = optionalUser.get();
		targetUser.setBlocked(true);
		userRepository.save(targetUser);

		attributes.addFlashAttribute("pass", "Blocked Success");
		return "redirect:/manage-users";
	}

	@Override
	public String unBlockUser(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<User> optionalUser = userRepository.findById(id);

		// Target user not found
		if (optionalUser.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		User targetUser = optionalUser.get();
		targetUser.setBlocked(false);
		userRepository.save(targetUser);

		attributes.addFlashAttribute("pass", "Un-Blocked Success");
		return "redirect:/manage-users";
	}

	private User getUserFromSession(HttpSession session) {
		return session != null ? (User) session.getAttribute("user") : null;
	}

	@Override
	public String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		List<Theater> theaters = theaterRepository.findAll();
		map.put("theaters", theaters);

		return "manage-theaters.html";
	}

	@Override
	public String loadAddTheater(HttpSession session, RedirectAttributes attributes, TheaterDto theaterDto) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		return "add-theater.html";
	}

	@Override
	public String addTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result) throws IOException {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		// Uniqueness check
		if (theaterRepository.existsByNameAndAddress(theaterDto.getName(), theaterDto.getAddress())) {

			result.rejectValue("name", "error.name", "* Theater Already Exists");
		}

		// Image validation
		MultipartFile image = theaterDto.getImage();
		if (image == null || image.isEmpty()) {
			result.rejectValue("image", "error.image", "* Image is Required");
		}

		// Validation failure
		if (result.hasErrors()) {
			return "add-theater.html";
		}

		Theater theater = new Theater();
		theater.setName(theaterDto.getName());
		theater.setAddress(theaterDto.getAddress());
		theater.setLocationLink(theaterDto.getLocationLink());
		theater.setImageLocation(cloudinaryHelper.getTheaterImageLink(image));

		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Theater Added Successfully");
		return "redirect:/manage-theaters";
	}

	@Override
	public String deleteTheater(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		// Theater not found
		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Theater theater = optionalTheater.get();

		// Screen dependency check
		if (theater.getScreenCount() != 0) {
			attributes.addFlashAttribute("fail", "First Remove The Screens to Remove Theater");
			return "redirect:/manage-theaters";
		}

		theaterRepository.delete(theater);
		attributes.addFlashAttribute("pass", "Theater Removed Success");

		return "redirect:/manage-theaters";
	}

	@Override
	public String editTheater(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();

		TheaterDto theaterDto = new TheaterDto(theater.getName(), theater.getAddress(), theater.getLocationLink(),
				null);

		map.put("id", theater.getId());
		map.put("imageLink", theater.getImageLocation());
		map.put("theaterDto", theaterDto);

		return "edit-theater.html";
	}

	@Override
	public String updateTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result, Long id) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();
		theater.setName(theaterDto.getName());
		theater.setAddress(theaterDto.getAddress());
		theater.setLocationLink(theaterDto.getLocationLink());

		MultipartFile image = theaterDto.getImage();

		// 🔥 Cloudinary logic (same as addTheater)
		if (image != null && !image.isEmpty()) {
			String imageUrl = cloudinaryHelper.getTheaterImageLink(image);
			theater.setImageLocation(imageUrl);
		}
		// else → keep existing image (no change needed)

		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Theater Updated Successfully");
		return "redirect:/manage-theaters";
	}

	@Override
	public String manageScreens(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();
		List<Screen> screens = screenRepository.findByTheater(theater);

		map.put("screens", screens);
		map.put("id", id);

		return "manage-screens.html";
	}

	@Override
	public String addScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map,
			ScreenDto screenDto) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(id);

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		screenDto.setTheaterId(id);
		map.put("screenDto", screenDto);

		return "add-screen.html";
	}

	@Override
	public String addScreen(ScreenDto screenDto, BindingResult result, HttpSession session,
			RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Theater> optionalTheater = theaterRepository.findById(screenDto.getTheaterId());

		if (optionalTheater.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Theater");
			return "redirect:/manage-theaters";
		}

		Theater theater = optionalTheater.get();

		// Uniqueness check
		if (screenRepository.existsByNameAndTheater(screenDto.getName(), theater)) {

			result.rejectValue("name", "error.name", "* Screen Already Exist in The Theater");
		}

		if (result.hasErrors()) {
			return "add-screen.html";
		}

		Screen screen = new Screen();
		screen.setName(screenDto.getName());
		screen.setType(screenDto.getType());
		screen.setTheater(theater);

		screenRepository.save(screen);

		theater.setScreenCount(theater.getScreenCount() + 1);
		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Screen Added Success");
		return "redirect:/manage-screens/" + theater.getId();
	}

	@Override
	public String deleteScreen(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();
		Theater theater = screen.getTheater();

		// Dependency check
		if (showRepository.existsByScreen(screen)) {
			attributes.addFlashAttribute("fail", "There are Shows Runing You can not Delete");
			return "redirect:/manage-screens/" + theater.getId();
		}

		// Remove seats first
		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);
		seatRepository.deleteAll(seats);

		// Update theater screen count
		theater.setScreenCount(theater.getScreenCount() - 1);
		theaterRepository.save(theater);

		screenRepository.delete(screen);

		attributes.addFlashAttribute("pass", "Screen Removed Success");
		return "redirect:/manage-screens/" + theater.getId();
	}

	@Override
	public String editScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();

		ScreenDto screenDto = new ScreenDto(screen.getName(), screen.getType(), screen.getTheater().getId());

		map.put("screenDto", screenDto);
		map.put("id", screen.getId());

		return "edit-screen.html";
	}

	@Override
	public String updateScreen(@Valid ScreenDto screenDto, Long id, BindingResult result, HttpSession session,
			RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		if (result.hasErrors()) {
			map.put("id", id);
			return "edit-screen.html";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();
		screen.setName(screenDto.getName());
		screen.setType(screenDto.getType());

		screenRepository.save(screen);

		attributes.addFlashAttribute("pass", "Screen Updated Success");
		return "redirect:/manage-screens/" + screen.getTheater().getId();
	}

	@Override
	public String manageSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();

		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);

		// Group seats by row (preserves order)
		Map<String, List<Seat>> seatsByRow = seats.stream()
				.collect(Collectors.groupingBy(Seat::getSeatRow, LinkedHashMap::new, Collectors.toList()));

		map.put("seatsByRow", seatsByRow);
		map.put("screenId", id);

		return "manage-seats";
	}

	@Override
	public String addSeats(Long id, HttpSession session, ModelMap map, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		map.put("id", id);
		map.put("seatLayoutForm", new SeatLayoutForm());

		return "add-seats.html";
	}

	@Override
	public String saveSeats(Long screenId, SeatLayoutForm form, HttpSession session, RedirectAttributes attributes) {

		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Screen screen = screenRepository.findById(screenId).orElseThrow();

		for (SeatRowDto row : form.getRows()) {
			for (int i = 1; i <= row.getTotalSeats(); i++) {

				Seat seat = new Seat();
				seat.setScreen(screen);
				seat.setSeatRow(row.getRowName());
				seat.setSeatColumn(i);
				seat.setSeatNumber(row.getRowName() + i);
				seat.setCategory(row.getCategory());

				seatRepository.save(seat);
			}
		}

		attributes.addFlashAttribute("success", "Seats added successfully");
		return "redirect:/manage-screens/" + screen.getTheater().getId();
	}

	@Override
	public String manageMovies(HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		List<Movie> movies = movieRepository.findAll();
		map.put("movies", movies);

		return "manage-movies.html";
	}

	@Override
	public String loadAddMovie(MovieDto movieDto, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		return "add-movie.html";
	}

	@Override
	public String addMovie(MovieDto movieDto, BindingResult result, RedirectAttributes attributes,
			HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		// Duplicate movie check
		if (movieRepository.existsByNameAndReleaseDate(movieDto.getName(), movieDto.getReleaseDate())) {

			result.rejectValue("name", "error.name", "* Movie Already Exists");
		}

		// Image validation
		if (movieDto.getImage() == null || movieDto.getImage().isEmpty()) {
			result.rejectValue("image", "error.image", "* Image is Required");
		}

		if (result.hasErrors()) {
			return "add-movie.html";
		}

		Movie movie = new Movie(null, movieDto.getName(), movieDto.getLanguages(), movieDto.getGenre(),
				movieDto.getDuration(), cloudinaryHelper.generateImageLink(movieDto.getImage()),
				movieDto.getTrailerLink(), movieDto.getDescription(), movieDto.getReleaseDate(), movieDto.getCast());

		movieRepository.save(movie);

		attributes.addFlashAttribute("pass", "Movie Added Success");
		return "redirect:/manage-movies";
	}

	@Override
	public String manageShows(Long id, ModelMap map, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();
		List<Show> shows = showRepository.findByScreen(screen);

		map.put("shows", shows);
		map.put("id", id);

		return "manage-shows";
	}

	@Override
	public String addShow(Long id, ModelMap map, RedirectAttributes attributes, HttpSession session) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Screen> optionalScreen = screenRepository.findById(id);

		if (optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Screen");
			return "redirect:/manage-theaters";
		}

		Screen screen = optionalScreen.get();

		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);
		List<Movie> movies = movieRepository.findAll();

		if (seats.isEmpty() || movies.isEmpty()) {
			attributes.addFlashAttribute("fail", "First Add Movie and Add Seat Layout to continue");
			return "redirect:/manage-screens/" + screen.getTheater().getId();
		}

		ShowDto showDto = new ShowDto();
		showDto.setScreenId(screen.getId());

		map.put("movies", movies);
		map.put("showDto", showDto);

		return "add-show";
	}

	@Override
	public String addShow(ShowDto showDto, BindingResult result, RedirectAttributes attributes, HttpSession session,
			ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Movie> optionalMovie = movieRepository.findById(showDto.getMovieId());
		Optional<Screen> optionalScreen = screenRepository.findById(showDto.getScreenId());

		if (optionalMovie.isEmpty() || optionalScreen.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Data");
			return "redirect:/manage-theaters";
		}

		Movie movie = optionalMovie.get();
		Screen screen = optionalScreen.get();

		// Show date validation
		if (showDto.getShowDate().isBefore(movie.getReleaseDate())) {
			result.rejectValue("showDate", "error.showDate", "* Show Date Should be After Movie Release");
		}

		// Time overlap validation
		List<Show> existingShows = showRepository.findByScreen(screen);
		for (Show show : existingShows) {
			if (show.getShowDate().isEqual(showDto.getShowDate())
					&& showDto.getStartTime().isBefore(show.getEndTime())) {

				result.rejectValue("startTime", "error.startTime", "* In Same Time There is One More Show");
				break;
			}
		}

		if (result.hasErrors()) {
			map.put("movies", movieRepository.findAll());
			return "add-show";
		}

		// Create show
		Show show = new Show();
		show.setMovie(movie);
		show.setScreen(screen);
		show.setShowDate(showDto.getShowDate());
		show.setStartTime(showDto.getStartTime());
		show.setTicketPrice(showDto.getTicketPrice());

		show.setEndTime(show.getStartTime().plusHours(movie.getDuration().getHour())
				.plusMinutes(movie.getDuration().getMinute() + 30));

		// Create show seats
		List<ShowSeat> showSeats = new ArrayList<>();
		List<Seat> seats = seatRepository.findByScreenOrderBySeatRowAscSeatColumnAsc(screen);

		for (Seat seat : seats) {
			ShowSeat showSeat = new ShowSeat();
			showSeat.setSeat(seat);
			showSeat.setBooked(false);
			showSeats.add(showSeat);
		}

		show.setSeats(showSeats);
		showRepository.save(show);

		attributes.addFlashAttribute("pass", "Show Added Success");
		return "redirect:/manage-shows/" + showDto.getScreenId();
	}

	@Override
	public String loadMain(ModelMap map) {

		Set<Movie> movies = showRepository.findByShowDateAfter(LocalDate.now().minusDays(1)).stream()
				.map(Show::getMovie).collect(Collectors.toSet());

		map.put("movies", movies);
		return "main";
	}

	@Override
	public String bookMovie(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		Optional<Movie> optionalMovie = movieRepository.findById(id);

		if (optionalMovie.isEmpty()) {
			attributes.addFlashAttribute("fail", "Movie not found");
			return "redirect:/main";
		}

		Movie movie = optionalMovie.get();

		List<String> showDates = showRepository.findByMovieAndShowDateAfter(movie, LocalDate.now().minusDays(1))
				.stream().map(Show::getShowDate).distinct().sorted().map(LocalDate::toString).toList();

		map.put("movie", movie);
		map.put("showDate", showDates);

		return "display-shows";
	}

	@Override
	public String deleteShow(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Show> optionalShow = showRepository.findById(id);

		if (optionalShow.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Show");
			return "redirect:/manage-movies";
		}

		Show show = optionalShow.get();
		Long screenId = show.getScreen().getId();

		showRepository.delete(show);

		attributes.addFlashAttribute("pass", "Show Removed Success");
		return "redirect:/manage-shows/" + screenId;
	}

	@Override
	public String deleteMovie(Long id, HttpSession session, RedirectAttributes attributes) {

		User loggedInUser = getUserFromSession(session);

		// Authorization check
		if (loggedInUser == null || !"ADMIN".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		Optional<Movie> optionalMovie = movieRepository.findById(id);

		if (optionalMovie.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Movie");
			return "redirect:/manage-movies";
		}

		Movie movie = optionalMovie.get();

		// Dependency check
		if (showRepository.existsByMovie(movie)) {
			attributes.addFlashAttribute("fail", "There are Shwos Running So can not Delete");
			return "redirect:/manage-movies";
		}

		movieRepository.delete(movie);

		attributes.addFlashAttribute("pass", "Movie Removed Success");
		return "redirect:/manage-movies";
	}

	@Override
	public String displayShowsOnDate(LocalDate date, Long movieId, RedirectAttributes attributes, ModelMap map) {

		Optional<Movie> optionalMovie = movieRepository.findById(movieId);

		if (optionalMovie.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Movie");
			return "redirect:/main";
		}

		Movie movie = optionalMovie.get();

		List<Show> shows = showRepository.findByShowDateAndMovie(date, movie);

		// Group shows by theater
		Map<Theater, List<Show>> theaters = shows.stream().collect(
				Collectors.groupingBy(show -> show.getScreen().getTheater(), LinkedHashMap::new, Collectors.toList()));

		map.put("theaters", theaters);
		return "display-theaters.html";
	}

	@Override
	public String showSeats(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {

		User loggedInUser = getUserFromSession(session);

		// User authorization
		if (loggedInUser == null || !"USER".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Login to Continue Booking");
			return "redirect:/login";
		}

		Optional<Show> optionalShow = showRepository.findById(id);

		if (optionalShow.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Show");
			return "redirect:/main";
		}

		Show show = optionalShow.get();

		// Group show seats by row (preserves UI order)
		Map<String, List<ShowSeat>> seatsByRow = show.getSeats().stream()
				.collect(Collectors.groupingBy(s -> s.getSeat().getSeatRow(), LinkedHashMap::new, Collectors.toList()));

		map.put("seatsByRow", seatsByRow);
		map.put("showId", id);

		return "select-seats";
	}

	@Override
	public String confirmBooking(Long showId, Long[] seatIds, HttpSession session, ModelMap map,
			RedirectAttributes attributes) throws RazorpayException {

		User loggedInUser = getUserFromSession(session);

		// Authorization
		if (loggedInUser == null || !"USER".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Login to Continue Booking");
			return "redirect:/login";
		}

		// Seat validation
		if (seatIds == null || seatIds.length == 0) {
			attributes.addFlashAttribute("fail", "Please select at least one seat");
			return "redirect:/show-seats/" + showId;
		}

		Optional<Show> optionalShow = showRepository.findById(showId);

		if (optionalShow.isEmpty()) {
			attributes.addFlashAttribute("fail", "Invalid Show");
			return "redirect:/main";
		}

		Show show = optionalShow.get();

		// Selected seat IDs
		Set<Long> selectedSeatIds = Set.of(seatIds);

		// Filter selected seats
		List<ShowSeat> selectedSeats = show.getSeats().stream().filter(seat -> selectedSeatIds.contains(seat.getId()))
				.toList();

		double amount = show.getTicketPrice() * selectedSeats.size();

		// Razorpay order
		RazorpayClient razorpay = new RazorpayClient(RAZORPAY_KEY, RAZORPAY_SECRET);

		JSONObject orderRequest = new JSONObject();
		orderRequest.put("amount", amount * 100);
		orderRequest.put("currency", "INR");

		Order order = razorpay.orders.create(orderRequest);
		String orderId = order.get("id");

		// UI attributes
		map.put("key", RAZORPAY_KEY);
		map.put("amount", amount * 100);
		map.put("currency", "INR");
		map.put("orderId", orderId);
		map.put("show", show);
		map.put("showSeats", selectedSeats);
		map.put("user", loggedInUser);

		// Prepare ticket (temporary)
		BookedTicket ticket = new BookedTicket();
		ticket.setMovieName(show.getMovie().getName());
		ticket.setOrderId(orderId);
		ticket.setScreenName(show.getScreen().getName());
		ticket.setTheaterName(show.getScreen().getTheater().getName());
		ticket.setShowDate(show.getShowDate().toString());
		ticket.setShowTiming(show.getStartTime().toString());
		ticket.setTicketPrice(show.getTicketPrice());
		ticket.setShowId(showId);

		String[] seatNumbers = selectedSeats.stream().map(seat -> seat.getSeat().getSeatNumber())
				.toArray(String[]::new);

		ticket.setSeatNumber(seatNumbers);
		ticket.setTicketCount(seatNumbers.length);

		// Save in Redis (temporary)
		redisService.saveTicket(orderId, ticket);

		return "confirm-ticket";
	}

	@Override
	public String confirmTicket(HttpSession session, ModelMap map, RedirectAttributes attributes,
			String razorpay_order_id, String razorpay_payment_id) throws IOException, WriterException {

		User loggedInUser = getUserFromSession(session);

		// Authorization
		if (loggedInUser == null || !"USER".equals(loggedInUser.getRole())) {
			attributes.addFlashAttribute("fail", "Login to Continue Booking");
			return "redirect:/login";
		}

		BookedTicket ticket = redisService.getTicket(razorpay_order_id);

		if (ticket == null) {
			attributes.addFlashAttribute("fail", "Something Went Wrong try Again");
			return "redirect:/login";
		}

		// Finalize ticket
		ticket.setPaymentId(razorpay_payment_id);
		ticket.setUser(loggedInUser);

		// Generate QR
		byte[] qr = qrHelper.createQr(ticket.getMovieName() + "-" + ticket.getTheaterName() + "-"
				+ ticket.getShowTiming() + "-" + Arrays.toString(ticket.getSeatNumber()));

		ticket.setQrUrl(cloudinaryHelper.saveTicketQr(qr));
		ticketRepository.save(ticket);

		// Mark seats as booked
		Show show = showRepository.findById(ticket.getShowId()).orElseThrow();

		Set<String> bookedSeatNumbers = Set.of(ticket.getSeatNumber());

		for (ShowSeat seat : show.getSeats()) {
			if (bookedSeatNumbers.contains(seat.getSeat().getSeatNumber())) {
				seat.setBooked(true);
				showSeatRepository.save(seat);
			}
		}

		map.put("ticket", ticket);
		return "view-ticket.html";
	}

}
