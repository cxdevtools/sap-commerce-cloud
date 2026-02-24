package me.cxdev.commerce.toolkit.testing.itemmodel;

import java.util.List;
import java.util.Locale;

interface ItemModelAttribute extends Cloneable {
	String getKey();

	Object getValue();

	Object getOriginalValue();

	void setValue(Object value);

	Object getValue(Locale locale);

	Object getOriginalValue(Locale locale);

	void setValue(Locale locale, Object value);

	ItemModelAttribute clone();

	boolean isDirty();

	boolean isDirty(Locale locale);

	List<Locale> getDirtyLocales();

	void save();

	void reload();
}
