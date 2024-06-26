package ru.ravel.qrref.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

import java.nio.file.Files
import java.nio.file.Path

@Service
class QrService {

	private final int height = 600
	private final int width = 600
	private final String format = "png"
	private final String url = System.getenv("url")


	private void createQrFile(String data, Path path) throws WriterException, IOException {
		Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class)
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8")
		hints.put(EncodeHintType.MARGIN, 1)
		BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints)
		MatrixToImageWriter.writeToPath(matrix, format, path)
	}

	void getQr(String key, HttpServletResponse response) {
		try {
			Path path = Path.of("${key}.${format}")
			String data = "${url}/?key=${key}"
			createQrFile(data, path)
			response.setContentType(MediaType.IMAGE_PNG_VALUE)
			Files.copy(path, response.getOutputStream())
			path.toFile().delete()
		} catch (WriterException | IOException e) {
			e.printStackTrace()
		}
	}

}
