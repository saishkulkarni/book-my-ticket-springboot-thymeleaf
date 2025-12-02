package com.jsp.book.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.PasswordDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.User;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.AES;
import com.jsp.book.util.EmailHelper;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final SecureRandom random;
	private final EmailHelper emailHelper;
	private final RedisService redisService;

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
				if(user.isBlocked()) {
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

}
