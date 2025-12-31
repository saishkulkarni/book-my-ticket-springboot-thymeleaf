package com.jsp.book.service;

import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.BookedTicket;

public interface RedisService {
	void saveUserDto(String email, UserDto userDto);

	void saveOtp(String email, int otp);

	UserDto getUserDto(String email);

	int getOtp(String email);

	void saveTicket(String orderId, BookedTicket ticket);

	BookedTicket getTicket(String orderId);
}
