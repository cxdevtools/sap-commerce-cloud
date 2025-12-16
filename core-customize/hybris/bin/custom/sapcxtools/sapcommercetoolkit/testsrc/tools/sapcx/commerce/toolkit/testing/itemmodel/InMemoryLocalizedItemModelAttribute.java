package tools.sapcx.commerce.toolkit.testing.itemmodel;

import static tools.sapcx.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.getDefaultLocale;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

class InMemoryLocalizedItemModelAttribute implements ItemModelAttribute {
	/**
	 * List of already processed objects for use by recursive calls.
	 *
	 * @see #equals
	 */
	private static final ThreadLocal<Set<Object>> SEEN = new ThreadLocal<>();

	private String key;
	private Map<Locale, Object> localizedValues = new HashMap<>();
	private Map<Locale, Object> localizedOriginalValues = null;

	InMemoryLocalizedItemModelAttribute(String key) {
		this.key = key;
	}

	InMemoryLocalizedItemModelAttribute(String key, Map<Locale, Object> localizedValues) {
		this.key = key;
		if (localizedValues != null) {
			this.localizedOriginalValues = new HashMap<>();
			this.localizedValues.putAll(localizedValues);
		}
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Object getValue() {
		return getValue(null);
	}

	@Override
	public Object getOriginalValue() {
		return getOriginalValue(null);
	}

	@Override
	public void setValue(Object value) {
		setValue(null, value);
	}

	@Override
	public Object getValue(Locale locale) {
		return localizedValues.get(getLocaleWithFallback(locale));
	}

	@Override
	public Object getOriginalValue(Locale locale) {
		return isDirty() ? localizedOriginalValues.get(getLocaleWithFallback(locale)) : null;
	}

	@Override
	public void setValue(Locale locale, Object value) {
		if (!isDirty()) {
			localizedOriginalValues = new HashMap<>(localizedValues);
		}
		localizedValues.put(getLocaleWithFallback(locale), value);
	}

	@Override
	public boolean isDirty() {
		return localizedOriginalValues != null;
	}

	@Override
	public boolean isDirty(Locale locale) {
		return isDirty() && !Objects.equals(getValue(locale), getOriginalValue(locale));
	}

	@Override
	public List<Locale> getDirtyLocales() {
		return !isDirty() ? List.of()
				: localizedValues.keySet().stream()
						.filter(this::isDirty)
						.collect(Collectors.toList());
	}

	@Override
	public void save() {
		localizedOriginalValues = null;
	}

	@Override
	public void reload() {
		if (isDirty()) {
			localizedValues.clear();
			localizedValues.putAll(localizedOriginalValues);
			save();
		}
	}

	private Locale getLocaleWithFallback(Locale locale) {
		return locale != null ? locale : getDefaultLocale();
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
	private boolean actualEquals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		InMemoryLocalizedItemModelAttribute that = (InMemoryLocalizedItemModelAttribute) o;
		return Objects.equals(key, that.key) && Objects.equals(localizedValues, that.localizedValues);
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
				return Objects.hash(key, localizedValues);
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
		return new ToStringBuilder(this)
				.append("key", key)
				.append("localizedValues", localizedValues)
				.toString();
	}

	@Override
	public InMemoryLocalizedItemModelAttribute clone() {
		try {
			InMemoryLocalizedItemModelAttribute clone = (InMemoryLocalizedItemModelAttribute) super.clone();
			clone.key = key;
			clone.localizedValues = new HashMap<>(localizedValues);
			return clone;
		} catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex);
		}
	}
}
