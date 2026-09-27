package com.ikincieniyi.urlshortener.link;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
class LinkCodeGenerator {

	private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int CODE_LENGTH = 8;

	private final SecureRandom random = new SecureRandom();

	String generate() {
		StringBuilder code = new StringBuilder(CODE_LENGTH);
		for (int i = 0; i < CODE_LENGTH; i++) {
			code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
		}
		return code.toString();
	}
}
