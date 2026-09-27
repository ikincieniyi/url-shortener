package com.ikincieniyi.urlshortener.link;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
class LinkRedirectController {

	private final LinkRepository repository;

	LinkRedirectController(LinkRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/{code:[A-Za-z0-9]{8}}")
	ResponseEntity<Void> redirect(@PathVariable String code) {
		Link link = repository.findByCode(code).orElse(null);
		if (link == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Short link not found");
		}
		return ResponseEntity.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, link.getOriginalUrl())
				.build();
	}
}
