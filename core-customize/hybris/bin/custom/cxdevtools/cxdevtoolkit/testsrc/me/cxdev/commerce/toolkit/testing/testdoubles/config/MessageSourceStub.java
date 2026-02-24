package me.cxdev.commerce.toolkit.testing.testdoubles.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

public class MessageSourceStub implements MessageSource {
	public static MessageSourceStub always(String message) {
		MessageSourceStub stub = new MessageSourceStub();
		stub.fixedMessage = message;
		return stub;
	}

	public static MessageSourceStub alwaysForLocale(String message, Locale locale) {
		MessageSourceStub stub = new MessageSourceStub();
		stub.fixedMessage = message;
		stub.onlyForLocale = locale;
		return stub;
	}

	public static MessageSourceStub stubForLocale(Map<String, String> messages, Locale locale) {
		MessageSourceStub stub = new MessageSourceStub();
		stub.onlyForLocale = locale;
		stub.messages.put(locale, messages);
		return stub;
	}

	public static MessageSourceStub.Builder messageSource() {
		return new MessageSourceStub.Builder();
	}

	private String fixedMessage;
	private Locale onlyForLocale;
	private Map<Locale, Map<String, String>> messages = new HashMap<>();

	private MessageSourceStub() {
	}

	@Override
	public String getMessage(String code, Object[] arguments, Locale locale) throws NoSuchMessageException {
		if (onlyForLocale != null && !onlyForLocale.equals(locale)) {
			return null;
		}

		if (fixedMessage != null && onlyForLocale == null) {
			return String.format(fixedMessage, arguments);
		}

		Map<String, String> localMessages = messages.get(locale);
		if (localMessages != null && localMessages.containsKey(code)) {
			return String.format(localMessages.get(code), arguments);
		}

		return null;
	}

	@Override
	public String getMessage(String code, Object[] arguments, String defaultMessage, Locale locale) {
		String message = getMessage(code, arguments, locale);
		return (message != null) ? message : defaultMessage;
	}

	@Override
	public String getMessage(MessageSourceResolvable messageSourceResolvable, Locale locale) throws NoSuchMessageException {
		String[] codes = messageSourceResolvable.getCodes();
		Object[] arguments = messageSourceResolvable.getArguments();
		return Arrays.stream(codes != null ? codes : new String[0])
				.map(code -> getMessage(code, arguments, locale))
				.filter(Objects::nonNull)
				.findFirst()
				.orElseGet(messageSourceResolvable::getDefaultMessage);
	}

	public static class Builder {
		private MessageSourceStub messageSource;

		private Builder() {
			messageSource = new MessageSourceStub();
		}

		private void assureLocaleExists(Locale locale) {
			if (!messageSource.messages.containsKey(locale)) {
				messageSource.messages.put(locale, new HashMap<>());
			}
		}

		public Builder addMessageForLocale(String key, String message, Locale locale) {
			assureLocaleExists(locale);
			messageSource.messages.get(locale).put(key, message);
			return this;
		}

		public Builder addMessagesForLocale(Map<String, String> messages, Locale locale) {
			assureLocaleExists(locale);
			messageSource.messages.get(locale).putAll(messages);
			return this;
		}

		public MessageSourceStub asStub() {
			return messageSource;
		}
	}
}
