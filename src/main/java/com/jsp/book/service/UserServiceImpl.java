package com.jsp.book.service;

import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jsp.book.dto.LoginDto;
import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.User;
import com.jsp.book.repository.UserRepository;
import com.jsp.book.util.AES;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	@Override
	public String register(UserDto userDto, BindingResult result) {
		if (!userDto.getPassword().equals(userDto.getConfirmPassword()))
			result.rejectValue("confirmPassword", "error.confirmPassword",
					"* Password and ConfirmPassword Should be Same");
		if (result.hasErrors())
			return "register.html";
		else {
			return "main.html";
		}
	}

	@Override
	public String login(LoginDto dto, RedirectAttributes attributes,HttpSession session) {
		User user = userRepository.findByEmail(dto.getEmail());
		if (user == null) {
			attributes.addFlashAttribute("fail", "Invalid Email");
			return "redirect:/login";
		} else {
			if (AES.decrypt(user.getPassword()).equals(dto.getPassword())) {
				session.setAttribute("user", user);
				attributes.addFlashAttribute("pass", "Login Success");
				return "redirect:/";
			} else {
				attributes.addFlashAttribute("fail", "Invalid Password");
				return "redirect:/login";
			}
		}
	}

}
