package me.cxdev.commerce.toolkit.session;

import de.hybris.platform.servicelayer.session.SessionService;

@FunctionalInterface
public interface LocalViewExecutionBody<T> {
	default T execute(SessionService sessionService) {
		return execute();
	}

	T execute();
}
