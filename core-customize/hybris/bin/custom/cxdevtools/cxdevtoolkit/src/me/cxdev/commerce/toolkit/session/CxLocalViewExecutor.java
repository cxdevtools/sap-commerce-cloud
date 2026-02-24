package me.cxdev.commerce.toolkit.session;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.Map;

import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

/**
 * This class is used to execute code in a local view. It basically wraps the execution of the method
 * {@link SessionService#executeInLocalViewWithParams(Map, SessionExecutionBody)} by providing a more convenient
 * approach using a lambda expression.
 */
public class CxLocalViewExecutor implements LocalViewExecutor {
	private SessionService sessionService;

	public CxLocalViewExecutor(SessionService sessionService) {
		this.sessionService = sessionService;
	}

	public <T> T with(Map<String, Object> parameter, LocalViewExecutionBody<T> code) {
		return sessionService.executeInLocalViewWithParams(emptyIfNull(parameter), new SessionExecutionBody() {
			@Override
			public Object execute() {
				return code.execute(sessionService);
			}
		});
	}
}
