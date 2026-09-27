package com.ikincieniyi.urlshortener.link;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class LinkController {

	private final LinkService service;
	private final String baseUrl;

	LinkController(LinkService service, @Value("${app.base-url}") String baseUrl) {
		this.service = service;
		this.baseUrl = baseUrl;
	}

	@PostMapping("/api/links")
	ResponseEntity<CreateLinkResponse> create(@Valid @RequestBody CreateLinkRequest request) {
		Link link = service.createLink(request.originalUrl());
		String shortUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + link.getCode();
		return ResponseEntity.created(URI.create(shortUrl))
				.body(new CreateLinkResponse(link.getCode(), shortUrl));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail invalidOriginalUrl() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"originalUrl must be a non-blank absolute HTTP(S) URL with a host and at most 2048 characters.");
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ProblemDetail unreadableRequest() {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Request body must be valid JSON with originalUrl.");
	}

	record CreateLinkRequest(@NotBlank @Size(max = 2048) @HttpUrl String originalUrl) {
	}

	record CreateLinkResponse(String code, String shortUrl) {
	}
}
