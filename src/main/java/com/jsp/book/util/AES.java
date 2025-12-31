package com.jsp.book.util;

import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class AES {

	private static final String SECRET_KEY = "pioqwbautwqnmsduoi";
	private static final String SALT_VALUE = "uasbnasddoiuqw";

	private static final String KEY_DERIVATION_ALGO = "PBKDF2WithHmacSHA256";
	private static final String CIPHER_ALGO = "AES/CBC/PKCS5Padding";
	private static final int ITERATIONS = 65536;
	private static final int KEY_SIZE = 256;

	private static final byte[] IV = new byte[16];

	private AES() {
	}

	public static String encrypt(String plainText) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(IV));

			byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

			return Base64.getEncoder().encodeToString(encrypted);

		} catch (Exception ex) {
			System.err.println("Error occurred during encryption: " + ex.getMessage());
			return null;
		}
	}

	public static String decrypt(String encryptedText) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(IV));

			byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));

			return new String(decrypted, StandardCharsets.UTF_8);

		} catch (Exception ex) {
			System.err.println("Error occurred during decryption: " + ex.getMessage());
			return null;
		}
	}

	private static SecretKeySpec getSecretKey() throws Exception {

		SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGO);

		KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT_VALUE.getBytes(StandardCharsets.UTF_8), ITERATIONS,
				KEY_SIZE);

		SecretKey tmp = factory.generateSecret(spec);
		return new SecretKeySpec(tmp.getEncoded(), "AES");
	}
}
