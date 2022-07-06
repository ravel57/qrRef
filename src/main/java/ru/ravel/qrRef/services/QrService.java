package ru.ravel.qrRef.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;


@Service
public class QrService {

    private int height = 600;
    private int width = 600;
    private String format = "png";


    private void createQrFile(String data, Path path) throws WriterException, IOException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height, hints);
        MatrixToImageWriter.writeToPath(matrix, format, path);
    }

    public void getQr(String key, HttpServletResponse response) {
        try {
            Path path = Path.of(String.format("%s.%s", key, format));
            String data = String.format("%s/?key=%s", System.getenv("url"), key);
            createQrFile(data, path);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            Files.copy(path, response.getOutputStream());
            path.toFile().delete();
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }
    }

}