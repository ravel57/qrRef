package ru.ravel.qrRef.controllers;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.ravel.qrRef.services.KeyService;
import ru.ravel.qrRef.services.QrService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class MainController {

    @Autowired
    QrService qrService;
    @Autowired
    KeyService keyService;

    @GetMapping("/")
    public String getRootMapping()  {
        return "Main";
    }

    @GetMapping("/qr")
    public ResponseEntity<Object> getQrMapping(HttpServletResponse response) throws IOException, WriterException {
        String data = keyService.generateKey(20);
        String path = data + ".png";
        Path imgFile = qrService.createQrFile("qrref:" + data, path, 400, 400);
        response.setContentType(MediaType.IMAGE_JPEG_VALUE);
        Files.copy(imgFile, response.getOutputStream());
        imgFile.toFile().delete();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/qrref:{key}")
    public ResponseEntity<Object> getKeyMapping(@PathVariable String key) {
        keyService.sendStrToFront(key);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}