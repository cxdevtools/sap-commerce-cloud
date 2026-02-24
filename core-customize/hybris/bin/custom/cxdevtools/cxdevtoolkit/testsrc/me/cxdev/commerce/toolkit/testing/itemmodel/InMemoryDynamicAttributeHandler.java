package me.cxdev.commerce.toolkit.testing.itemmodel;

import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

public class InMemoryDynamicAttributeHandler<VALUE, T extends AbstractItemModel> implements DynamicAttributeHandler<VALUE, T> {
	private VALUE value;

	public InMemoryDynamicAttributeHandler(VALUE value) {
		this.value = value;
	}

	@Override
	public VALUE get(T model) {
		return value;
	}

	@Override
	public void set(T model, VALUE value) {
		this.value = value;
	}
}
