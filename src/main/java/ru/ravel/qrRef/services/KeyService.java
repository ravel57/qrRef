package ru.ravel.qrRef.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.ravel.qrRef.dto.Message;

import java.util.Random;

@Service
public class KeyService {

    @Autowired
    private SimpMessagingTemplate simpMessaging;

    public String generateKey(int keyLength) {
        Random r = new Random();
        String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            outStr.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
        }
        return outStr.toString();
    }

    public void sendStrToFront(Message message) {
        simpMessaging.convertAndSend("/topic/activity", message);
    }

}
