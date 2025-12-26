package com.jsp.book.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
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
	public byte[] qrCreator(String text) throws IOException, WriterException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200, hints);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "png", bos);
		byte[] imageBytes = bos.toByteArray();
		return imageBytes;
	}
}
