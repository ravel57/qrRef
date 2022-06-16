package ru.ravel.qrRef.services;

import com.google.zxing.BarcodeFormat;
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


@Service
public class QrService {

    private Path createQrFile(String data, String path, int height, int width) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height);
        Path file = Path.of(path);
        MatrixToImageWriter.writeToPath(matrix, path.substring(path.lastIndexOf('.') + 1), file);
        return file;
    }

    public void getQr(String key, HttpServletResponse response) throws IOException, WriterException {
        String path = key + ".png";
        Path imgFile = createQrFile("qrref:" + key, path, 400, 400);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        Files.copy(imgFile, response.getOutputStream());
        imgFile.toFile().delete();
    }

}