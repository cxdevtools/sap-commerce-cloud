package me.cxdev.commerce.toolkit.testing.itemmodel;

import static me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.attributeFor;
import static me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.localizedAttributeFor;

import java.io.ObjectStreamException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ItemModelInternalContext;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

class InMemoryItemModelContext implements ItemModelInternalContext, InMemoryItemModelContextAccessor {
	private static AtomicLong nextPk = new AtomicLong(System.currentTimeMillis());

	private long persistenceVersion = 0L;
	private PK pk;
	private PK newPK;
	private String itemType;
	private int typeCode = -1;
	private Map<String, ItemModelAttribute> attributes = new HashMap<>();

	static InMemoryItemModelContext contextWithAttributes(Class<? extends ItemModel> itemType) {
		String simpleName = itemType.getSimpleName().replaceAll("Model$", "");
		return new InMemoryItemModelContext(simpleName);
	}

	static InMemoryItemModelContext contextWithAttributes(Class<? extends ItemModel> itemType, int typeCode) {
		String simpleName = itemType.getSimpleName().replaceAll("Model$", "");
		return new InMemoryItemModelContext(simpleName, typeCode);
	}

	/**
	 * Returns getNextPk with the leftmost 17 bits masked, i.e. only the rightmost 47 bits.
	 *
	 * In PK.createPK_Counter() the counter value is left shifted by 16 bits, causing it to become
	 * negative if the 17th bit happens to be a one, which is not allowed.
	 */
	private static long getNextPkForPKCounter() {
		return getNextPk() & 0x7FFFFFFFFFFFL;
	}

	private static long getNextPk() {
		return nextPk.getAndIncrement();
	}

	private InMemoryItemModelContext(String itemType) {
		this.itemType = itemType;
	}

	private InMemoryItemModelContext(String itemType, int typeCode) {
		this.itemType = itemType;
		this.typeCode = typeCode;
	}

	InMemoryItemModelContext copy() {
		InMemoryItemModelContext clone = new InMemoryItemModelContext(itemType, typeCode);
		clone.attributes = attributes.values().stream()
				.collect(Collectors.toMap(ItemModelAttribute::getKey, ItemModelAttribute::clone));
		return clone;
	}

	void updateItemAfterCopy(AbstractItemModel newItem) {
		attributes.values().stream()
				.filter(InMemoryItemAwareAttribute.class::isInstance)
				.map(InMemoryItemAwareAttribute.class::cast)
				.forEach(attribute -> attribute.updateItem(newItem));
	}

	public void throwExceptionForAttribute(String key, Throwable throwable) {
		attributes.put(key, new ExceptionThrowingItemModelAttribute(key, throwable));
	}

	@Override
	public String getItemType() {
		return itemType;
	}

	@Override
	public String getTenantId() {
		return "junit";
	}

	@Override
	public <T> T getValue(String s, T t) {
		return attributes.containsKey(s) ? (T) attributes.get(s).getValue() : t;
	}

	@Override
	public <T> void setPropertyValue(String attribute, T value) {
		this.setValue(attribute, value);
	}

	@Override
	public <T> T getPropertyValue(String attribute) {
		return getValue(attribute, null);
	}

	@Override
	public <T> T setValue(String s, T t) {
		if (attributes.containsKey(s)) {
			attributes.get(s).setValue(t);
		} else {
			attributes.put(s, attributeFor(s, t));
		}
		return t;
	}

	@Override
	public <T> T getLocalizedValue(String s, Locale locale) {
		return attributes.containsKey(s) ? (T) attributes.get(s).getValue(locale) : null;
	}

	@Override
	public <T> T getLocalizedRelationValue(String s, Locale locale) {
		return attributes.containsKey(s) ? (T) attributes.get(s).getValue(locale) : null;
	}

	@Override
	public <T> void setLocalizedValue(String s, Locale locale, T t) {
		if (attributes.containsKey(s)) {
			attributes.get(s).setValue(locale, t);
		} else if (t == null) {
			attributes.put(s, localizedAttributeFor(s, Object.class).withValue(locale, t).build());
		} else {
			attributes.put(s, localizedAttributeFor(s, (Class<T>) t.getClass()).withValue(locale, t).build());
		}
	}

	@Override
	public PK getPK() {
		return pk;
	}

	@Override
	public <T> T getDynamicValue(AbstractItemModel abstractItemModel, String s) {
		return attributes.containsKey(s) ? (T) attributes.get(s).getValue() : null;
	}

	@Override
	public <T> void setDynamicValue(AbstractItemModel abstractItemModel, String s, T t) {
		if (attributes.containsKey(s)) {
			attributes.get(s).setValue(t);
		} else {
			attributes.put(s, new InMemoryDynamicItemModelAttribute<>(s, abstractItemModel, t));
		}
	}

	/**
	 * If more complex logic is required within the testing, a handler for dynamic attributes can be registered.
	 */
	public <T extends ItemModel, VALUE> void setDynamicHandler(T item, String key, DynamicAttributeHandler<VALUE, T> handler) {
		this.attributes.put(key, new InMemoryDynamicItemModelAttribute(key, item, handler));
	}

	@Override
	public long getPersistenceVersion() {
		return persistenceVersion;
	}

	@Override
	public boolean exists() {
		return !isNew() && !isRemoved();
	}

	@Override
	public boolean isNew() {
		return pk == null;
	}

	@Override
	public boolean isRemoved() {
		throw new UnsupportedOperationException("This method is not supported with testing, yet");
	}

	@Override
	public boolean isUpToDate() {
		return !isNew() && !isDirty();
	}

	@Override
	public boolean isDirty() {
		return attributes.values().stream().map(ItemModelAttribute::isDirty).anyMatch(Boolean.TRUE::equals);
	}

	@Override
	public boolean isDirty(String s) {
		return attributes.containsKey(s) && attributes.get(s).isDirty();
	}

	@Override
	public boolean isDirty(String s, Locale locale) {
		return attributes.containsKey(s) && attributes.get(s).isDirty();
	}

	@Override
	public boolean isLoaded(String s) {
		throw new UnsupportedOperationException("This method is not supported with testing, yet");
	}

	@Override
	public boolean isLoaded(String s, Locale locale) {
		throw new UnsupportedOperationException("This method is not supported with testing, yet");
	}

	@Override
	public boolean isDynamicAttribute(String attributeName) {
		return (attributes.containsKey(attributeName) && attributes.get(attributeName) instanceof InMemoryDynamicItemModelAttribute);
	}

	@Override
	public Object loadOriginalValue(String s) {
		throw new UnsupportedOperationException("loadOriginalValue not supported by InMenoryItemModelContext, yet!");
	}

	@Override
	public Object loadOriginalValue(String s, Locale locale) {
		throw new UnsupportedOperationException("loadOriginalValue not supported by InMenoryItemModelContext, yet!");
	}

	@Override
	public void unloadAttribute(final String s) {
		throw new UnsupportedOperationException("unloadAttribute not supported by InMenoryItemModelContext, yet!");
	}

	@Override
	public <T> T getOriginalValue(String s) {
		return attributes.containsKey(s) ? (T) attributes.get(s).getOriginalValue() : null;
	}

	@Override
	public <T> T getOriginalValue(String s, Locale locale) {
		return attributes.containsKey(s) ? (T) attributes.get(s).getOriginalValue(locale) : null;
	}

	@Override
	public Set<String> getDirtyAttributes() {
		return attributes.values().stream()
				.filter(ItemModelAttribute::isDirty)
				.map(ItemModelAttribute::getKey)
				.collect(Collectors.toSet());
	}

	@Override
	public Map<Locale, Set<String>> getDirtyLocalizedAttributes() {
		Set<Locale> dirtyLocales = attributes.values().stream()
				.flatMap(a -> a.getDirtyLocales().stream())
				.collect(Collectors.toSet());

		Map<Locale, Set<String>> dirtyLocalizedAttributes = new HashMap<>();
		for (Locale dirtyLocale : dirtyLocales) {
			Set<String> dirtyAttributes = attributes.values().stream()
					.filter(a -> a.isDirty(dirtyLocale))
					.map(ItemModelAttribute::getKey)
					.collect(Collectors.toSet());
			dirtyLocalizedAttributes.put(dirtyLocale, dirtyAttributes);
		}
		return dirtyLocalizedAttributes;
	}

	@Override
	public Object getSource() {
		throw new UnsupportedOperationException("This method is not supported with testing, yet");
	}

	@Override
	public <T> T getLocalizedDynamicValue(AbstractItemModel abstractItemModel, String s, Locale locale) {
		ItemModelAttribute itemModelAttribute = attributes.get(s);
		if (itemModelAttribute instanceof InMemoryDynamicItemModelAttribute) {
			return (T) itemModelAttribute.getValue(locale);
		} else {
			throw new UnsupportedOperationException("No dynamic attribute handler registered for attribute " + s);
		}
	}

	@Override
	public <T> void setLocalizedDynamicValue(AbstractItemModel abstractItemModel, String s, Locale locale, T t) {
		ItemModelAttribute itemModelAttribute = attributes.get(s);
		if (itemModelAttribute instanceof InMemoryDynamicItemModelAttribute) {
			itemModelAttribute.setValue(locale, t);
		} else {
			throw new UnsupportedOperationException("No dynamic attribute handler registered for attribute " + s);
		}
	}

	@Override
	public Object writeReplace(Object o) throws ObjectStreamException {
		throw new UnsupportedOperationException("This method is not supported with testing, yet");
	}

	@Override
	public void save() {
		if (isNew()) {
			this.pk = generateNewPK();
		}
		persistenceVersion++;

		attributes.values().stream().forEach(ItemModelAttribute::save);
	}

	@Override
	public void refresh() {
		attributes.values().stream().forEach(ItemModelAttribute::reload);
	}

	@Override
	public PK getNewPK() {
		return newPK;
	}

	@Override
	public PK generateNewPK() {
		if (newPK == null) {
			if (!isNew()) {
				throw new IllegalStateException("Could not generate new PK for model which is not new.");
			}

			if (typeCode != -1) {
				newPK = PK.createFixedCounterPK(typeCode, getNextPkForPKCounter());
			} else {
				newPK = PK.fromLong(getNextPk());
			}
		}
		return newPK;
	}

	@Override
	public int hashCode(AbstractItemModel abstractItemModel) {
		return abstractItemModel.getItemModelContext().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InMemoryItemModelContext that = (InMemoryItemModelContext) o;
		if (pk != null && pk.equals(that.pk))
			return true;
		return Objects.equals(itemType, that.itemType) && Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return (pk != null) ? pk.hashCode() : Objects.hash(itemType, attributes);
	}
}
