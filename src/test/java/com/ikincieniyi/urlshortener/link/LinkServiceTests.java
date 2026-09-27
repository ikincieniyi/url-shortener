package com.ikincieniyi.urlshortener.link;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTests {

	@Mock
	private LinkRepository repository;

	@Mock
	private LinkCodeGenerator codeGenerator;

	@InjectMocks
	private LinkService service;

	@Test
	void retriesOnlyWhenLinkCodeConstraintCollides() {
		when(codeGenerator.generate()).thenReturn("SameCode", "NewCode1");
		when(repository.saveAndFlush(any(Link.class)))
				.thenThrow(constraintViolation("links_code_key", "23505"))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Link link = service.createLink("https://example.com");

		assertEquals("NewCode1", link.getCode());
		verify(repository, times(2)).saveAndFlush(any(Link.class));
	}

	@Test
	void repeatedOriginalUrlGetsANewCode() {
		when(codeGenerator.generate()).thenReturn("First001", "Second01");
		when(repository.saveAndFlush(any(Link.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		Link first = service.createLink("https://example.com");
		Link second = service.createLink("https://example.com");

		assertEquals("First001", first.getCode());
		assertEquals("Second01", second.getCode());
		verify(repository, times(2)).saveAndFlush(any(Link.class));
	}

	@Test
	void unrelatedDatabaseErrorIsNotRetried() {
		DataIntegrityViolationException error = constraintViolation("other_constraint", "23505");
		when(repository.saveAndFlush(any(Link.class))).thenThrow(error);

		assertSame(error, assertThrows(DataIntegrityViolationException.class,
				() -> service.createLink("https://example.com")));
		verify(repository).saveAndFlush(any(Link.class));
	}

	@Test
	void stopsAfterFiveCodeCollisions() {
		when(repository.saveAndFlush(any(Link.class)))
				.thenThrow(constraintViolation("links_code_key", "23505"));

		assertThrows(IllegalStateException.class, () -> service.createLink("https://example.com"));
		verify(repository, times(5)).saveAndFlush(any(Link.class));
	}

	private DataIntegrityViolationException constraintViolation(String constraintName, String sqlState) {
		SQLException sqlException = new SQLException("constraint violation", sqlState);
		ConstraintViolationException hibernateException =
				new ConstraintViolationException("constraint violation", sqlException, constraintName);
		return new DataIntegrityViolationException("constraint violation", hibernateException);
	}
}
