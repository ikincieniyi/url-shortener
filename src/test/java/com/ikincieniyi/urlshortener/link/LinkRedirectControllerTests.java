package com.ikincieniyi.urlshortener.link;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkRedirectController.class)
class LinkRedirectControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LinkRepository repository;

	@Test
	void redirectsKnownCodeToUnchangedOriginalUrl() throws Exception {
		String originalUrl = "https://example.com/path?item=1%2F2#part";
		when(repository.findByCode("Ab12Cd34"))
				.thenReturn(Optional.of(new Link("Ab12Cd34", originalUrl)));

		mockMvc.perform(get("/Ab12Cd34"))
				.andExpect(status().isFound())
				.andExpect(header().string(HttpHeaders.LOCATION, originalUrl));

		verify(repository).findByCode("Ab12Cd34");
		verifyNoMoreInteractions(repository);
	}

	@Test
	void returnsNotFoundForUnknownCode() throws Exception {
		when(repository.findByCode("Missing1")).thenReturn(Optional.empty());

		mockMvc.perform(get("/Missing1"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.title").value("Not Found"))
				.andExpect(jsonPath("$.detail").value("Short link not found"));

		verify(repository).findByCode("Missing1");
		verifyNoMoreInteractions(repository);
	}

	@Test
	void ignoresPathsThatAreNotEightAlphanumericCharacters() throws Exception {
		for (String path : new String[] { "/short", "/bad_code", "/123456789" }) {
			mockMvc.perform(get(path))
					.andExpect(status().isNotFound());
		}

		verifyNoInteractions(repository);
	}
}
