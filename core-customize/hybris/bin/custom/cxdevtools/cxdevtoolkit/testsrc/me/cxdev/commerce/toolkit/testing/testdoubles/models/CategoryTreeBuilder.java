package me.cxdev.commerce.toolkit.testing.testdoubles.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.hybris.platform.category.model.CategoryModel;

public class CategoryTreeBuilder {
	public static CategoryTreeBuilder categoryTree() {
		return new CategoryTreeBuilder();
	}

	private List<CategoryBuilder<? extends CategoryModel>> rootCategories = new ArrayList<>();

	private CategoryTreeBuilder() {
	}

	public CategoryTreeBuilder addRoot(CategoryBuilder<? extends CategoryModel> rootCategory) {
		this.rootCategories.add(rootCategory);
		return this;
	}

	public List<? extends CategoryModel> asModel() {
		return rootCategories.stream()
				.map(CategoryBuilder::asModel)
				.collect(Collectors.toList());
	}
}
