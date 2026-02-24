package me.cxdev.commerce.toolkit.testing.testdoubles.event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

import org.springframework.context.ApplicationListener;

public class EventServiceSpy implements EventService {
	private List<ApplicationListener> registeredEventListeners = new ArrayList<>();
	private List<ApplicationListener> unregisteredEventListeners = new ArrayList<>();
	private Set<ApplicationListener> effectiveEventListeners = new HashSet<>();
	private List<AbstractEvent> publishedEvents = new ArrayList<>();
	private int publishedEventsCounter = 0;

	@Override
	public void publishEvent(AbstractEvent abstractEvent) {
		publishedEvents.add(abstractEvent);
	}

	@Override
	public boolean registerEventListener(ApplicationListener applicationListener) {
		effectiveEventListeners.add(applicationListener);
		return registeredEventListeners.add(applicationListener);
	}

	@Override
	public boolean unregisterEventListener(ApplicationListener applicationListener) {
		effectiveEventListeners.remove(applicationListener);
		return unregisteredEventListeners.add(applicationListener);
	}

	@Override
	public Set<ApplicationListener> getEventListeners() {
		return effectiveEventListeners;
	}

	public List<AbstractEvent> getAllPublishedEvents() {
		return new ArrayList<>(publishedEvents);
	}

	public <T> T getPublishedEvent() {
		return (T) publishedEvents.get(publishedEventsCounter++);
	}

	public <T> T getPublishedEvent(int index) {
		return (T) publishedEvents.get(index);
	}
}
