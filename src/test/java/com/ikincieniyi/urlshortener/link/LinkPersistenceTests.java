package com.ikincieniyi.urlshortener.link;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LinkPersistenceTests {

	@Autowired
	private LinkRepository repository;

	@Autowired
	private LinkService service;

	@Autowired
	private MockMvc mockMvc;

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

	@Test
	void redirectsSavedLinkWithoutChangingDatabaseRow() throws Exception {
		String originalUrl = "https://example.com/path?item=1%2F2#part";
		Link saved = repository.saveAndFlush(new Link(unusedCode(), originalUrl));
		try {
			Link before = repository.findByCode(saved.getCode()).orElseThrow();
			long countBefore = repository.count();

			mockMvc.perform(get("/" + saved.getCode()))
					.andExpect(status().isFound())
					.andExpect(header().string(HttpHeaders.LOCATION, originalUrl));

			Link after = repository.findByCode(saved.getCode()).orElseThrow();
			assertEquals(countBefore, repository.count());
			assertEquals(before.getId(), after.getId());
			assertEquals(before.getOriginalUrl(), after.getOriginalUrl());
			assertEquals(before.getCreatedAt(), after.getCreatedAt());
		} finally {
			repository.deleteById(saved.getId());
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
