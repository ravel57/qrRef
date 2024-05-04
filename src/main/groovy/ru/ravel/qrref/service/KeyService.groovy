package ru.ravel.qrref.service

import org.springframework.stereotype.Service

@Service
class KeyService {
	private final static String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"

	static String generateKey(int keyLength) {
		Random r = new Random()
		StringBuilder outStr = new StringBuilder()
		for (i in 0..<keyLength) {
			outStr.append(ALPHABET.charAt(r.nextInt(ALPHABET.length())))
		}
		return outStr.toString()
	}

}
