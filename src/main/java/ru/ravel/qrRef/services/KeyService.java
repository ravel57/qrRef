package ru.ravel.qrRef.services;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class KeyService {

    public String generateKey(int keyLength) {
        Random r = new Random();
        String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < keyLength; i++) {
            outStr.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
        }
        return outStr.toString();
    }

}
