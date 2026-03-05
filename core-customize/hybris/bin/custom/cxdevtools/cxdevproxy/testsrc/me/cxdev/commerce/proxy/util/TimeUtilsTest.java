package me.cxdev.commerce.proxy.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class TimeUtilsTest {
	@Test
	void testParseDuration_WithSeconds() {
		assertEquals(5000L, TimeUtils.parseIntervalToMillis("5s"));
		assertEquals(3600000L, TimeUtils.parseIntervalToMillis("3600s"));
	}

	@Test
	void testParseDuration_WithMinutes() {
		assertEquals(60000L, TimeUtils.parseIntervalToMillis("1m"));
		assertEquals(3600000L, TimeUtils.parseIntervalToMillis("60m"));
	}

	@Test
	void testParseDuration_WithHours() {
		assertEquals(3600000L, TimeUtils.parseIntervalToMillis("1h"));
		assertEquals(36000000L, TimeUtils.parseIntervalToMillis("10h"));
	}

	@Test
	void testParseDuration_WithDays() {
		assertEquals(86400000L, TimeUtils.parseIntervalToMillis("1d"));
	}

	@Test
	void testParseDuration_WithMilliseconds() {
		assertEquals(500L, TimeUtils.parseIntervalToMillis("500ms"));
	}

	@Test
	void testParseDuration_WithRawNumber_DefaultsToMillis() {
		assertEquals(800L, TimeUtils.parseIntervalToMillis("800"));
	}

	@Test
	void testParseDuration_WithNullOrEmpty_ReturnsZero() {
		assertEquals(0L, TimeUtils.parseIntervalToMillis(null));
		assertEquals(0L, TimeUtils.parseIntervalToMillis(""));
		assertEquals(0L, TimeUtils.parseIntervalToMillis("   "));
	}

	@Test
	void testParseDuration_WithInvalidFormat_ThrowsException() {
		assertThrows(NumberFormatException.class, () -> {
			TimeUtils.parseIntervalToMillis("10x");
		}, "Should throw an exception for unknown time units");

		assertThrows(NumberFormatException.class, () -> {
			TimeUtils.parseIntervalToMillis("abc");
		}, "Should throw an exception for completely invalid formats");
	}
}
