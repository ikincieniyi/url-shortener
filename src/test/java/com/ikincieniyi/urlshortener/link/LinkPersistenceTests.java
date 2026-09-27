package com.ikincieniyi.urlshortener.link;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest
class LinkPersistenceTests {

	@Autowired
	private LinkRepository repository;

	@Autowired
	private LinkService service;

	@MockitoBean
	private LinkCodeGenerator codeGenerator;

	@Test
	void retriesAnActualPostgresCodeCollision() {
		String collidingCode = unusedCode();
		String replacementCode = unusedCode();
		while (replacementCode.equals(collidingCode)) {
			replacementCode = unusedCode();
		}
		when(codeGenerator.generate()).thenReturn(collidingCode, replacementCode);

		Link existing = repository.saveAndFlush(new Link(collidingCode, "https://example.com/existing"));
		Long createdId = null;
		try {
			Link created = service.createLink("https://example.com/new");
			createdId = created.getId();
			assertNotNull(createdId);
			assertEquals(replacementCode, created.getCode());
			assertEquals(createdId, repository.findByCode(replacementCode).orElseThrow().getId());
		} finally {
			if (createdId != null) {
				repository.deleteById(createdId);
			}
			repository.deleteById(existing.getId());
		}
	}

	private String unusedCode() {
		String code;
		do {
			code = UUID.randomUUID().toString().substring(0, 8);
		} while (repository.findByCode(code).isPresent());
		return code;
	}
}
