package com.ikincieniyi.urlshortener.link;

import java.net.URI;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

class HttpUrlValidator implements ConstraintValidator<HttpUrl, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true; // @NotBlank reports these values.
		}
		try {
			URI uri = URI.create(value);
			return uri.isAbsolute()
					&& ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
					&& uri.getHost() != null && !uri.getHost().isBlank();
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}
}
