package com.ikincieniyi.urlshortener.link;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
class LinkService {

	private static final int MAX_CODE_ATTEMPTS = 5;
	private static final String CODE_UNIQUE_CONSTRAINT = "links_code_key";

	private final LinkRepository repository;
	private final LinkCodeGenerator codeGenerator;

	LinkService(LinkRepository repository, LinkCodeGenerator codeGenerator) {
		this.repository = repository;
		this.codeGenerator = codeGenerator;
	}

	Link createLink(String originalUrl) {
		for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
			try {
				return repository.saveAndFlush(new Link(codeGenerator.generate(), originalUrl));
			} catch (DataIntegrityViolationException exception) {
				if (!isCodeCollision(exception)) {
					throw exception;
				}
				if (attempt == MAX_CODE_ATTEMPTS - 1) {
					throw new IllegalStateException("Could not generate a unique link code", exception);
				}
			}
		}
		throw new IllegalStateException("Could not generate a unique link code");
	}

	private boolean isCodeCollision(DataIntegrityViolationException exception) {
		for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConstraintViolationException violation
					&& "23505".equals(violation.getSQLState())
					&& CODE_UNIQUE_CONSTRAINT.equals(violation.getConstraintName())) {
				return true;
			}
		}
		return false;
	}
}
