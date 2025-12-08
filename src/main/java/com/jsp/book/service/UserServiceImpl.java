package com.jsp.book.service;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.ScreenDto;
import com.jsp.book.dto.TheaterDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.Screen;
import com.jsp.book.entity.Theater;
import com.jsp.book.entity.User;
import com.jsp.book.repository.ScreenRepository;
import com.jsp.book.repository.TheaterRepository;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.AES;
import com.jsp.book.util.EmailHelper;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final SecureRandom random;
	private final EmailHelper emailHelper;
	private final RedisService redisService;
	private final TheaterRepository theaterRepository;
	private final ScreenRepository screenRepository;

	@Override
	public String register(UserDto userDto, BindingResult result, RedirectAttributes attributes) {
		if (!userDto.getPassword().equals(userDto.getConfirmPassword()))
			result.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and ConfirmPassword Should be Same");
		if (userRepository.existsByEmail(userDto.getEmail()))
			result.rejectValue("email", "error.email", "* Email Should be unique");
		if (userRepository.existsByMobile(userDto.getMobile()))
			result.rejectValue("mobile", "error.mobile", "* Mobile Number Should be unique");

		if (result.hasErrors())
			return "register.html";

		else {
			int otp = random.nextInt(100000, 1000000);
			emailHelper.sendOtp(otp, userDto.getName(), userDto.getEmail());
			redisService.saveUserDto(userDto.getEmail(), userDto);
			redisService.saveOtp(userDto.getEmail(), otp);
			attributes.addFlashAttribute("pass", "Otp Sent Success");
			attributes.addFlashAttribute("email", userDto.getEmail());
			return "redirect:/otp";
		}
	}

	@Override
	public String login(LoginDto dto, RedirectAttributes attributes, HttpSession session) {
		User user = userRepository.findByEmail(dto.getEmail());
		if (user == null) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/login";
		} else {
			if (AES.decrypt(user.getPassword()).equals(dto.getPassword())) {
				if (user.isBlocked()) {
					attributes.addFlashAttribute("fail", "Account Blocked!, Contact Admin");
					return "redirect:/login";
				}
				session.setAttribute("user", user);
				attributes.addFlashAttribute("pass", "Login Success");
				return "redirect:/main";
			} else {
				attributes.addFlashAttribute("fail", "Invalid Password");
				return "redirect:/login";
			}
		}
	}

	@Override
	public String logout(HttpSession session, RedirectAttributes attributes) {
		session.removeAttribute("user");
		attributes.addFlashAttribute("pass", "Logout Success");
		return "redirect:/main";
	}

	@Override
	public String submitOtp(int otp, String email, RedirectAttributes attributes) {
		UserDto dto = redisService.getDtoByEmail(email);
		if (dto == null) {
			attributes.addFlashAttribute("fail", "Timeout Try Again Creating a New Account");
			return "redirect:/register";
		} else {
			int exOtp = redisService.getOtpByEmail(email);
			if (exOtp == 0) {
				attributes.addFlashAttribute("fail", "OTP Expired, Resend Otp and Try Again");
				attributes.addFlashAttribute("email", email);
				return "redirect:/otp";
			} else {
				if (otp == exOtp) {
					User user = new User(null, dto.getName(), dto.getEmail(), dto.getMobile(),
							AES.encrypt(dto.getPassword()), "USER", false);
					userRepository.save(user);
					attributes.addFlashAttribute("pass", "Account Registered Success");
					return "redirect:/main";

				} else {
					attributes.addFlashAttribute("fail", "Invalid OTP Try Again");
					attributes.addFlashAttribute("email", email);
					return "redirect:/otp";
				}
			}
		}
	}

	@Override
	public String resendOtp(String email, RedirectAttributes attributes) {
		UserDto dto = redisService.getDtoByEmail(email);
		if (dto == null) {
			attributes.addFlashAttribute("fail", "Timeout Try Again Creating a New Account");
			return "redirect:/register";
		} else {
			int otp = random.nextInt(100000, 1000000);
			emailHelper.sendOtp(otp, dto.getName(), dto.getEmail());
			redisService.saveOtp(dto.getEmail(), otp);
			attributes.addFlashAttribute("pass", "Otp Re-Sent Success");
			attributes.addFlashAttribute("email", dto.getEmail());
			return "redirect:/otp";
		}
	}

	@Override
	public String forgotPassword(String email, RedirectAttributes attributes) {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/forgot-password";
		} else {
			int otp = random.nextInt(100000, 1000000);
			emailHelper.sendOtp(otp, user.getName(), email);
			redisService.saveOtp(email, otp);
			attributes.addFlashAttribute("pass", "Sent Success");
			attributes.addFlashAttribute("email", email);
			return "redirect:/reset-password";
		}
	}

	@Override
	public String resetPassword(PasswordDto passwordDto, BindingResult result, RedirectAttributes attributes,
			ModelMap map) {
		if (result.hasErrors()) {
			map.put("email", passwordDto.getEmail());
			return "reset-password.html";
		}
		User user = userRepository.findByEmail(passwordDto.getEmail());
		if (user == null) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/forgot-password";
		} else {
			int exOtp = redisService.getOtpByEmail(passwordDto.getEmail());
			if (exOtp == 0) {
				attributes.addFlashAttribute("fail", "OTP Expired, Resend Otp and Try Again");
				attributes.addFlashAttribute("email", passwordDto.getEmail());
				return "redirect:/reset-password";
			} else {
				if (passwordDto.getOtp() == exOtp) {
					user.setPassword(AES.encrypt(passwordDto.getPassword()));
					userRepository.save(user);
					attributes.addFlashAttribute("pass", "Password Reset Success");
					return "redirect:/main";

				} else {
					attributes.addFlashAttribute("fail", "Invalid OTP Try Again");
					attributes.addFlashAttribute("email", passwordDto.getEmail());
					return "redirect:/reset-password";
				}
			}

		}
	}

	@Override
	public String manageUsers(HttpSession session, RedirectAttributes attributes, ModelMap map) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			List<User> users = userRepository.findByRole("USER");
			if (users.isEmpty()) {
				attributes.addFlashAttribute("fail", "No Users Registered Yet");
				return "redirect:/";
			} else {
				map.put("users", users);
				return "manage-users.html";
			}
		}
	}

	@Override
	public String blockUser(Long id, HttpSession session, RedirectAttributes attributes) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			User user1 = userRepository.findById(id).orElse(null);
			if (user1 == null) {
				attributes.addFlashAttribute("fail", "Invalid Session");
				return "redirect:/login";
			}
			user1.setBlocked(true);
			userRepository.save(user1);
			attributes.addFlashAttribute("pass", "Blocked Success");
			return "redirect:/manage-users";
		}
	}

	@Override
	public String unBlockUser(Long id, HttpSession session, RedirectAttributes attributes) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			User user1 = userRepository.findById(id).orElse(null);
			if (user1 == null) {
				attributes.addFlashAttribute("fail", "Invalid Session");
				return "redirect:/login";
			}
			user1.setBlocked(false);
			userRepository.save(user1);
			attributes.addFlashAttribute("pass", "Un-Blocked Success");
			return "redirect:/manage-users";
		}
	}

	private User getUserFromSession(HttpSession session) {
		return (User) session.getAttribute("user");
	}

	@Override
	public String manageTheater(ModelMap map, RedirectAttributes attributes, HttpSession session) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			List<Theater> theaters = theaterRepository.findAll();
			map.put("theaters", theaters);
			return "manage-theaters.html";
		}
	}

	@Override
	public String loadAddTheater(HttpSession session, RedirectAttributes attributes, TheaterDto theaterDto) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			return "add-theater.html";
		}
	}

	@Override
	public String addTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result) throws IOException {

		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		}

		if (theaterRepository.existsByNameAndAddress(theaterDto.getName(), theaterDto.getAddress())) {
			result.rejectValue("name", "error.name", "* Theater Already Exists");
		}

		MultipartFile image = theaterDto.getImage();
		if (image.isEmpty()) {
			result.rejectValue("image", "error.image", "* Image is Required");
		}

		if (result.hasErrors()) {
			return "add-theater.html";
		}

		String baseUploadDir = System.getProperty("user.dir") + "/uploads/theaters/";
		File directory = new File(baseUploadDir);
		if (!directory.exists())
			directory.mkdirs();

		String filename = theaterDto.getName() + image.getOriginalFilename();
		File destination = new File(directory, filename);
		image.transferTo(destination);

		Theater theater = new Theater();
		theater.setName(theaterDto.getName());
		theater.setAddress(theaterDto.getAddress());
		theater.setLocationLink(theaterDto.getLocationLink());
		theater.setImageLocation("/uploads/theaters/" + filename);

		theaterRepository.save(theater);

		attributes.addFlashAttribute("pass", "Theater Added Successfully");
		return "redirect:/manage-theaters";
	}

	@Override
	public String deleteTheater(Long id, HttpSession session, RedirectAttributes attributes) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			theaterRepository.deleteById(id);
			attributes.addFlashAttribute("pass", "Theater Removed Success");
			return "redirect:/manage-theaters";
		}
	}

	@Override
	public String editTheater(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Theater theater = theaterRepository.findById(id).orElse(null);
			TheaterDto theaterDto = new TheaterDto(theater.getName(), theater.getAddress(), theater.getLocationLink(),
					null);
			map.put("id", theater.getId());
			map.put("imageLink", theater.getImageLocation());
			map.put("theaterDto", theaterDto);
			return "edit-theater.html";
		}
	}

	@Override
	public String updateTheater(HttpSession session, RedirectAttributes attributes, @Valid TheaterDto theaterDto,
			BindingResult result, Long id) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Theater theater = new Theater();
			theater.setId(id);
			theater.setName(theaterDto.getName());
			theater.setAddress(theaterDto.getAddress());
			theater.setLocationLink(theaterDto.getLocationLink());

			MultipartFile image = theaterDto.getImage();
			if (image.isEmpty()) {
				theater.setImageLocation(theaterRepository.findById(id).get().getImageLocation());
			} else {
				String baseUploadDir = System.getProperty("user.dir") + "/uploads/theaters/";
				File directory = new File(baseUploadDir);
				if (!directory.exists())
					directory.mkdirs();
				String filename = theaterDto.getName() + image.getOriginalFilename();
				File destination = new File(directory, filename);
				try {
					image.transferTo(destination);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				theater.setImageLocation("/uploads/theaters/" + filename);
			}
			theaterRepository.save(theater);
			attributes.addFlashAttribute("pass", "Theater Updated Successfully");
			return "redirect:/manage-theaters";
		}
	}

	@Override
	public String manageScreens(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Theater theater = theaterRepository.findById(id).get();
			List<Screen> screens = screenRepository.findByTheater(theater);
			map.put("screens", screens);
			map.put("id", id);
			return "manage-screens.html";
		}
	}

	@Override
	public String addScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map,
			ScreenDto screenDto) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Theater theater = theaterRepository.findById(id).get();
			screenDto.setTheaterId(id);
			map.put("screenDto", screenDto);
			return "add-screen.html";
		}
	}

	@Override
	public String addScreen(ScreenDto screenDto, BindingResult result, HttpSession session,
			RedirectAttributes attributes) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Theater theater = theaterRepository.findById(screenDto.getTheaterId()).get();
			if (screenRepository.existsByNameAndTheater(screenDto.getName(), theater)) {
				result.rejectValue("name", "error.name", "* Screen Already Exist in The Theater");
			}
			if (result.hasErrors())
				return "add-screen.html";
			else {
				Screen screen = new Screen();
				screen.setName(screenDto.getName());
				screen.setTheater(theater);
				screen.setType(screenDto.getType());
				screenRepository.save(screen);
				theater.setScreenCount(theater.getScreenCount() + 1);
				theaterRepository.save(theater);
				attributes.addFlashAttribute("pass", "Screen Added Success");
				return "redirect:/manage-screens/" + theater.getId();
			}
		}
	}

	@Override
	public String deleteScreen(Long id, HttpSession session, RedirectAttributes attributes) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Screen screen = screenRepository.findById(id).orElseThrow();
			Theater theater = screen.getTheater();
			theater.setScreenCount(theater.getScreenCount() - 1);
			theaterRepository.save(theater);
			screenRepository.deleteById(id);
			attributes.addFlashAttribute("pass", "Screen Removed Success");
			return "redirect:/manage-screens/" + theater.getId();
		}
	}

	@Override
	public String editScreen(Long id, HttpSession session, RedirectAttributes attributes, ModelMap map) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			Screen screen = screenRepository.findById(id).orElseThrow();
			ScreenDto screenDto = new ScreenDto(screen.getName(), screen.getType(), screen.getTheater().getId());
			map.put("screenDto", screenDto);
			map.put("id", screen.getId());
			return "edit-screen.html";
		}
	}

	@Override
	public String updateScreen(@Valid ScreenDto screenDto, Long id, BindingResult result, HttpSession session,
			RedirectAttributes attributes, ModelMap map) {
		User user = getUserFromSession(session);
		if (user == null || !user.getRole().equals("ADMIN")) {
			attributes.addFlashAttribute("fail", "Invalid Session");
			return "redirect:/login";
		} else {
			if (result.hasErrors()) {
				map.put("id", id);
				return "edit-screen.html";
			}
			Screen screen = screenRepository.findById(id).orElseThrow();
			screen.setName(screenDto.getName());
			screen.setType(screenDto.getType());
			screenRepository.save(screen);
			attributes.addFlashAttribute("pass", "Screen Updated Success");
			return "redirect:/manage-screens/" + screen.getTheater().getId();
		}
	}

}
