package me.cxdev.commerce.toolkit.testing.itemmodel;

public interface InMemoryItemModelContextAccessor {
	void save();

	void refresh();

	boolean isNew();

	boolean isDirty();
}
