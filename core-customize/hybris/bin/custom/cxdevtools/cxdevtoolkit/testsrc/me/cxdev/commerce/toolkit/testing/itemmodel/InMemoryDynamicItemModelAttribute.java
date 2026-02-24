package me.cxdev.commerce.toolkit.testing.itemmodel;

import java.util.*;
import java.util.function.Function;

import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;
import de.hybris.platform.servicelayer.model.attribute.DynamicLocalizedAttributeHandler;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class InMemoryDynamicItemModelAttribute<T extends AbstractItemModel, VALUE> implements ItemModelAttribute, InMemoryItemAwareAttribute<T> {
	/**
	 * List of already processed objects for use by recursive calls.
	 *
	 * @see #equals
	 */
	private static final ThreadLocal<Set<Object>> SEEN = new ThreadLocal<>();

	private String key;
	private T item;
	private DynamicAttributeHandler<VALUE, T> handler;

	public InMemoryDynamicItemModelAttribute(String key, T item, VALUE value) {
		this(key, item, new InMemoryDynamicAttributeHandler<VALUE, T>(value));
	}

	public InMemoryDynamicItemModelAttribute(String key, T item, DynamicAttributeHandler<VALUE, T> handler) {
		this.key = key;
		this.item = item;
		this.handler = handler;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public VALUE getValue() {
		return handler.get(item);
	}

	@Override
	public Object getOriginalValue() {
		throw new UnsupportedOperationException("This method is not supported with dynamic attribute handlers");
	}

	@Override
	public void setValue(Object value) {
		handler.set(item, (VALUE) value);
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isDirty(Locale locale) {
		return false;
	}

	@Override
	public List<Locale> getDirtyLocales() {
		return List.of();
	}

	@Override
	public void save() {
	}

	@Override
	public void reload() {
	}

	@Override
	public Object getValue(Locale locale) {
		if (handler instanceof DynamicLocalizedAttributeHandler) {
			return ((DynamicLocalizedAttributeHandler<VALUE, T>) handler).get(item, locale);
		} else {
			return getValue();
		}
	}

	@Override
	public Object getOriginalValue(Locale locale) {
		throw new UnsupportedOperationException("This method is not supported with dynamic attribute handlers");
	}

	@Override
	public void setValue(Locale locale, Object value) {
		if (handler instanceof DynamicLocalizedAttributeHandler) {
			((DynamicLocalizedAttributeHandler<VALUE, T>) handler).set(item, (VALUE) value, locale);
		} else {
			setValue(value);
		}
	}

	/**
	 * The methods equals() and hashCode() on this class will recursively walk through the object
	 * graph of ItemModels to determine equality. If there are cyclic references in the object
	 * graph, that will lead to infinite recursion and cause a stack overflow.
	 *
	 * Any relation (in items.xml) creates references on both item types by default. With
	 * InMemoryItemModels the attribute on the other side is not set automatically, so in most cases
	 * this is not a problem, but if the test setup does setup both references for any reason, a
	 * cycle is created. For example, a base Product that holds a reference to a VariantProduct that
	 * in turn holds a reference to the base Product.
	 *
	 * To avoid the infinite recursion, a set of already seen (processed) objects has to be kept.
	 * The idiomatic way to do this would be to pass such a set on the stack (as a method
	 * parameter), but since the signatures of hashCode() and equals() are fixed, this is not
	 * possible.
	 *
	 * Therefore, we have to resort to keeping the set of seen objects in thread local storage. The
	 * first call to equals() or hashCode() will create the set, and, when returning from the
	 * recursion, remove it again, thus mimicking passing it on the stack, which is also "thread
	 * local".
	 *
	 * @see #SEEN
	 */
	@Override
	public boolean equals(Object o) {
		return executeWithSeen(obj -> {
			Set<Object> seen = SEEN.get();
			if (seen.contains(obj)) {
				return true;
			} else {
				seen.add(obj);
				return actualEquals(obj);
			}
		}, o);
	}

	/**
	 * The actual generated equals() implementation is here because it has to be wrapped with the
	 * handling of seen objects.
	 */
	private Boolean actualEquals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InMemoryDynamicItemModelAttribute that = (InMemoryDynamicItemModelAttribute) o;
		return Objects.equals(key, that.key) && Objects.equals(item, that.item) && Objects.equals(handler, that.handler);
	}

	/**
	 * @see #equals
	 */
	@Override
	public int hashCode() {
		return executeWithSeen(obj -> {
			Set<Object> set = SEEN.get();
			if (set.contains(obj)) {
				return key.hashCode();
			} else {
				set.add(obj);
				return Objects.hash(key, item, handler);
			}
		}, null);
	}

	/**
	 * Function wrapper keeping track of seen objects.
	 *
	 * @see #equals
	 */
	private <I, O> O executeWithSeen(Function<I, O> function, I input) {
		Set<Object> seen = SEEN.get();
		boolean recursionEntryPoint = false;
		if (seen == null) {
			SEEN.set(Collections.newSetFromMap(new IdentityHashMap<>()));
			recursionEntryPoint = true;
		}
		try {
			return function.apply(input);
		} finally {
			if (recursionEntryPoint) {
				SEEN.remove();
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("key", key).append("item", item).append("handler", handler).toString();
	}

	@Override
	public ItemModelAttribute clone() {
		try {
			return (ItemModelAttribute) super.clone();
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void updateItem(T item) {
		this.item = item;
	}
}
