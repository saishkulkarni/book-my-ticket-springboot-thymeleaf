package com.jsp.book.service;

import com.jsp.book.dto.UserDto;
import com.jsp.book.entity.BookedTicket;

public interface RedisService {

	void saveUserDto(String email, UserDto userDto);

	void saveOtp(String email, int otp);

	UserDto getDtoByEmail(String email);

	int getOtpByEmail(String email);

	void saveTicket(String id, BookedTicket ticket);

	BookedTicket getTicket(String razorpay_order_id);
	
}
