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
					.andExpect(status().isBadRequest());
		}
		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/links")
				.contentType(MediaType.APPLICATION_JSON)
				.content("null"))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(service);
	}
}
