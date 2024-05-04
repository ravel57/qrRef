package ru.ravel.qrref.service

import org.springframework.stereotype.Service

@Service
class KeyService {

	static String generateKey(int keyLength) {
		Random r = new Random();
		String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder outStr = new StringBuilder();
		for (int i = 0; i < keyLength; i++) {
			outStr.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())));
		}
		return outStr.toString();
	}

}
