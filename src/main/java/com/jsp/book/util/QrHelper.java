package com.jsp.book.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Component
public class QrHelper {

	private static final int QR_SIZE = 200;
	private static final String IMAGE_FORMAT = "png";
	private static final String CHARSET = "UTF-8";

	public byte[] createQr(String text) throws IOException, WriterException {

		QRCodeWriter qrCodeWriter = new QRCodeWriter();

		Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.CHARACTER_SET, CHARSET);

		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints);

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			MatrixToImageWriter.writeToStream(bitMatrix, IMAGE_FORMAT, outputStream);
			return outputStream.toByteArray();
		}
	}
}
