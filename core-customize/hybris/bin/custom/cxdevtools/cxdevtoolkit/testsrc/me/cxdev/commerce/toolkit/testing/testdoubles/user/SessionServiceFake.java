package me.cxdev.commerce.toolkit.testing.testdoubles.user;

import java.util.Map;

import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.session.MockSession;
import de.hybris.platform.servicelayer.session.Session;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;

import org.apache.commons.lang3.StringUtils;

public class SessionServiceFake implements SessionService {
	private MockSession session;

	@Override
	public Session getCurrentSession() {
		return session;
	}

	@Override
	public Session getSession(String id) {
		return hasCurrentSession() && StringUtils.equals(id, session.getSessionId()) ? session : null;
	}

	@Override
	public Session createNewSession() {
		return session = new MockSession();
	}

	@Override
	public void closeSession(Session session) {
		if (hasCurrentSession() && StringUtils.equals(session.getSessionId(), this.session.getSessionId())) {
			this.session = null;
		}
	}

	@Override
	public void closeCurrentSession() {
		if (hasCurrentSession()) {
			this.session = null;
		}
	}

	@Override
	public boolean hasCurrentSession() {
		return this.session != null;
	}

	@Override
	public void setAttribute(String name, Object value) {
		if (!hasCurrentSession()) {
			createNewSession();
		}
		getCurrentSession().setAttribute(name, value);
	}

	@Override
	public <T> T getAttribute(String name) {
		if (hasCurrentSession()) {
			return getCurrentSession().getAttribute(name);
		} else {
			return null;
		}
	}

	@Override
	public <T> T getOrLoadAttribute(String name, SessionAttributeLoader<T> sessionAttributeLoader) {
		T attributeValue = getAttribute(name);
		if (attributeValue != null) {
			return attributeValue;
		} else {
			setAttribute(name, sessionAttributeLoader.load());
			return getAttribute(name);
		}
	}

	@Override
	public <T> Map<String, T> getAllAttributes() {
		return (hasCurrentSession()) ? getCurrentSession().getAllAttributes() : Map.of();
	}

	@Override
	public <T> T executeInLocalView(SessionExecutionBody sessionExecutionBody) {
		return (T) sessionExecutionBody.execute();
	}

	@Override
	public <T> T executeInLocalViewWithParams(Map<String, Object> parameters, SessionExecutionBody sessionExecutionBody) {
		return (T) sessionExecutionBody.execute();
	}

	@Override
	public <T> T executeInLocalView(SessionExecutionBody sessionExecutionBody, UserModel user) {
		return (T) sessionExecutionBody.execute();
	}

	@Override
	public void removeAttribute(String name) {
		if (hasCurrentSession()) {
			getCurrentSession().removeAttribute(name);
		}
	}

	@Override
	public Session getBoundSession(Object object) {
		return hasCurrentSession() ? getCurrentSession() : null;
	}

	@Override
	public Object getRawSession(Session session) {
		return null;
	}
}
