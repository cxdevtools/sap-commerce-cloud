package me.cxdev.commerce.toolkit.session;

import java.util.Map;

import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

/**
 * This interface is used to execute code in a local view. It basically wraps the execution of the method
 * {@link SessionService#executeInLocalViewWithParams(Map, SessionExecutionBody)} by providing a more convenient
 * approach using a lambda expression.
 */
@FunctionalInterface
public interface LocalViewExecutor {
	<T> T with(Map<String, Object> parameter, LocalViewExecutionBody<T> code);
}
