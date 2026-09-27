package com.ikincieniyi.urlshortener.link;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class LinkController {

	private final LinkService service;
	private final String baseUrl;

	LinkController(LinkService service, @Value("${app.base-url}") String baseUrl) {
		this.service = service;
		this.baseUrl = baseUrl;
	}

	@PostMapping("/api/links")
	ResponseEntity<CreateLinkResponse> create(@RequestBody CreateLinkRequest request) {
		if (request == null || !isValidOriginalUrl(request.originalUrl())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "originalUrl must be an absolute HTTP(S) URL with a host and at most 2048 characters");
		}

		Link link = service.createLink(request.originalUrl());
		String shortUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + link.getCode();
		return ResponseEntity.created(URI.create(shortUrl))
				.body(new CreateLinkResponse(link.getCode(), shortUrl));
	}

	private boolean isValidOriginalUrl(String originalUrl) {
		if (originalUrl == null || originalUrl.length() > 2048) {
			return false;
		}
		try {
			URI uri = URI.create(originalUrl);
			return uri.isAbsolute()
					&& ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
					&& uri.getHost() != null && !uri.getHost().isBlank();
		} catch (IllegalArgumentException exception) {
			return false;
		}
	}

	record CreateLinkRequest(String originalUrl) {
	}

	record CreateLinkResponse(String code, String shortUrl) {
	}
}
