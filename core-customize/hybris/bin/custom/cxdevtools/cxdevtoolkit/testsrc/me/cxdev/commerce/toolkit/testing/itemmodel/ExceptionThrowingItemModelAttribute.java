package me.cxdev.commerce.toolkit.testing.itemmodel;

import java.util.Locale;

class ExceptionThrowingItemModelAttribute extends InMemoryItemModelAttribute {
	private Throwable throwable;

	ExceptionThrowingItemModelAttribute(String key, Throwable throwable) {
		super(key, null);
		this.throwable = throwable;
	}

	@Override
	public Object getValue() {
		throwExpectedThrowable();
		return super.getValue();
	}

	@Override
	public Object getOriginalValue() {
		throwExpectedThrowable();
		return super.getOriginalValue();
	}

	@Override
	public Object getOriginalValue(Locale locale) {
		throwExpectedThrowable();
		return super.getOriginalValue(locale);
	}

	@Override
	public Object getValue(Locale locale) {
		throwExpectedThrowable();
		return super.getValue(locale);
	}

	private void throwExpectedThrowable() {
		if (throwable instanceof RuntimeException) {
			throw (RuntimeException) throwable;
		} else {
			throw new RuntimeException(throwable);
		}
	}
}
