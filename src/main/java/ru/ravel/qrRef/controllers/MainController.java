package ru.ravel.qrRef.controllers;

import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ravel.qrRef.dto.Message;
import ru.ravel.qrRef.enums.messageType;
import ru.ravel.qrRef.services.KeyService;
import ru.ravel.qrRef.services.QrService;
import ru.ravel.qrRef.services.SocketService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class MainController {

    @Autowired
    QrService qrService;
    @Autowired
    KeyService keyService;
    @Autowired
    SocketService socketService;

    @GetMapping("/")
    public String getRoot() {
        return "Main";
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Object> getFavicon() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getKey")
    public ResponseEntity<Object> getKey() {
        String key = keyService.generateKey(20);
        return ResponseEntity.status(HttpStatus.OK).body(key);
    }

    @GetMapping("/getQr/{key}")
    public ResponseEntity<Object> getQr(HttpServletResponse response,
                                        @PathVariable String key) {
        qrService.getQr(key, response);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/qrRef:{key}")
    public ResponseEntity<Object> postKey(@PathVariable String key,
                                          @RequestParam String text) {
        Message message = Message.builder()
                .key(key)
                .message(text)
                .messageType(messageType.TEXT)
                .build();
        socketService.sendStrToFront(message);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}