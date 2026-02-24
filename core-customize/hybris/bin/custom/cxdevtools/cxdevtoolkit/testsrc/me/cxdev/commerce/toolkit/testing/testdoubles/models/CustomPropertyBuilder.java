package me.cxdev.commerce.toolkit.testing.testdoubles.models;

import de.hybris.platform.core.model.ItemModel;

public interface CustomPropertyBuilder<ITEM extends ItemModel> {
	String getPropertyName();

	void setAttributeValueOnModel(ITEM item);
}
