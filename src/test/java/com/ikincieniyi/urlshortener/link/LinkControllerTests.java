package com.ikincieniyi.urlshortener.link;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkController.class)
@TestPropertySource(properties = "app.base-url=https://sho.rt")
class LinkControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LinkService service;

	@Test
	void createsLinkWithLocationAndResponseBody() throws Exception {
		String originalUrl = "https://example.com/article";
		when(service.createLink(originalUrl)).thenReturn(new Link("Ab12Cd34", originalUrl));

		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"originalUrl\":\"https://example.com/article\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "https://sho.rt/Ab12Cd34"))
				.andExpect(jsonPath("$.code").value("Ab12Cd34"))
				.andExpect(jsonPath("$.shortUrl").value("https://sho.rt/Ab12Cd34"));
	}

	@Test
	void rejectsInvalidUrlsBeforeSaving() throws Exception {
		String[] invalidUrls = {
				"",
				"   ",
				"/relative/path",
				"ftp://example.com/file",
				"https:///missing-host",
				"not a url",
				"https://example.com/" + "a".repeat(2048)
		};

		for (String originalUrl : invalidUrls) {
			mockMvc.perform(post("/api/links")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"originalUrl\":\"" + originalUrl + "\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.title").value("Bad Request"))
					.andExpect(jsonPath("$.detail").value("originalUrl must be a non-blank absolute HTTP(S) URL with a host and at most 2048 characters."));
		}
		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.detail").value("originalUrl must be a non-blank absolute HTTP(S) URL with a host and at most 2048 characters."));
		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"originalUrl\":null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value("originalUrl must be a non-blank absolute HTTP(S) URL with a host and at most 2048 characters."));
		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("null"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.detail").value("Request body must be valid JSON with originalUrl."));

		verifyNoInteractions(service);
	}

	@Test
	void acceptsExactly2048CharactersWithoutChangingTheUrl() throws Exception {
		String originalUrl = "https://example.com/" + "a".repeat(2048 - "https://example.com/".length());
		when(service.createLink(originalUrl)).thenReturn(new Link("Ab12Cd34", originalUrl));

		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"originalUrl\":\"" + originalUrl + "\"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "https://sho.rt/Ab12Cd34"));
	}

	@Test
	void malformedJsonHasProblemDetailBody() throws Exception {
		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"originalUrl\":"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.detail").value("Request body must be valid JSON with originalUrl."));

		verifyNoInteractions(service);
	}
}
