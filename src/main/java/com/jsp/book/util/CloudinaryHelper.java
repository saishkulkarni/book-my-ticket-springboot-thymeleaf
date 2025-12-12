package com.jsp.book.util;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryHelper {

	@Value("${cloudinary.url}")
	private String url;

	@SuppressWarnings("unchecked")
	public String generateImageLink(MultipartFile file) {
		Cloudinary cloudinary = new Cloudinary(url);

		Map<String, Object> params = ObjectUtils.asMap("folder", "BMT-Movies", "use_filename", true);
		try {
			return (String) cloudinary.uploader().upload(file.getBytes(), params).get("url");
		} catch (IOException e) {
			e.printStackTrace();
			return "https://placehold.co/600x400/EEE/31343C";
		}
	}

	@SuppressWarnings("unchecked")
	public String getTheaterImageLink(MultipartFile file) {
		Cloudinary cloudinary = new Cloudinary(url);

		Map<String, Object> params = ObjectUtils.asMap("folder", "BMT-Theater", "use_filename", true);
		try {
			return (String) cloudinary.uploader().upload(file.getBytes(), params).get("url");
		} catch (IOException e) {
			e.printStackTrace();
			return "https://placehold.co/600x400/EEE/31343C";
		}
	}
}
