package me.cxdev.commerce.toolkit.testing.testdoubles.event;

import java.util.HashSet;
import java.util.Set;

import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import org.springframework.context.ApplicationListener;

public class EventServiceFake implements EventService {
	private Set<ApplicationListener> effectiveEventListeners = new HashSet<>();

	@Override
	public void publishEvent(AbstractEvent abstractEvent) {
		effectiveEventListeners.forEach(listener -> listener.onApplicationEvent(abstractEvent));
	}

	@Override
	public boolean registerEventListener(ApplicationListener applicationListener) {
		return effectiveEventListeners.add(applicationListener);
	}

	@Override
	public boolean unregisterEventListener(ApplicationListener applicationListener) {
		return effectiveEventListeners.remove(applicationListener);
	}

	@Override
	public Set<ApplicationListener> getEventListeners() {
		return new HashSet<>(effectiveEventListeners);
	}
}
