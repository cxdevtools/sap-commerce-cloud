package me.cxdev.commerce.toolkit.testing.itemmodel;

import de.hybris.platform.servicelayer.model.AbstractItemModel;

public interface InMemoryItemAwareAttribute<T extends AbstractItemModel> {
	void updateItem(T item);
}
