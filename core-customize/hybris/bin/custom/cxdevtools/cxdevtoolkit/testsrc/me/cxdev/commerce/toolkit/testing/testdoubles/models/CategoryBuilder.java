package me.cxdev.commerce.toolkit.testing.testdoubles.models;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.attribute.DynamicAttributeHandler;

import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

public class CategoryBuilder<ITEM extends CategoryModel> {
	public static CategoryBuilder<CategoryModel> category(String code) {
		return category(code, CategoryModel.class);
	}

	public static <T extends CategoryModel> CategoryBuilder<T> category(String code, Class<T> itemClass) {
		return new CategoryBuilder<>(code, itemClass);
	}

	public static CategoryBuilder<CategoryModel> sampleCategory() {
		return category("sample")
				.name("Sample Category")
				.description("Description for Sample Category");
	}

	private ITEM category;
	private List<CustomPropertyBuilder<ITEM>> customProperties = new ArrayList<>();
	private List<CategoryBuilder<? extends CategoryModel>> subCategories = new ArrayList<>();
	private List<ProductBuilder<? extends ProductModel>> products = new ArrayList<>();

	private CategoryBuilder(String code, Class<ITEM> itemClass) {
		this.category = InMemoryModelFactory.createTestableItemModel(itemClass, Map.of(CategoryModel.ALLSUPERCATEGORIES, AllSuperCategories.INSTANCE));
		this.category.setCode(code);
	}

	public CategoryBuilder<ITEM> code(String code) {
		this.category.setCode(code);
		return this;
	}

	public CategoryBuilder<ITEM> name(String name) {
		this.category.setName(name);
		return this;
	}

	public CategoryBuilder<ITEM> description(String description) {
		this.category.setDescription(description);
		return this;
	}

	public CategoryBuilder<ITEM> customProperty(CustomPropertyBuilder<ITEM> custom) {
		customProperties.add(custom);
		return this;
	}

	public CategoryBuilder<ITEM> addChild(CategoryBuilder<? extends CategoryModel> builder) {
		this.subCategories.add(builder);
		return this;
	}

	public CategoryBuilder<ITEM> addChild(ProductBuilder<? extends ProductModel> builder) {
		this.products.add(builder);
		return this;
	}

	public ITEM asModel() {
		ITEM clone = InMemoryModelFactory.copy(this.category);
		customProperties.stream().forEach(c -> c.setAttributeValueOnModel(clone));

		clone.setProducts(products.stream().map(ProductBuilder::asModel).collect(Collectors.toList()));
		for (ProductModel product : clone.getProducts()) {
			ArrayList<CategoryModel> categories = new ArrayList<>(emptyIfNull(product.getSupercategories()));
			categories.add(clone);
			product.setSupercategories(categories);
		}

		clone.setCategories(subCategories.stream().map(CategoryBuilder::asModel).collect(Collectors.toList()));
		for (CategoryModel category : clone.getCategories()) {
			ArrayList<CategoryModel> categories = new ArrayList<>(emptyIfNull(category.getSupercategories()));
			categories.add(clone);
			category.setSupercategories(categories);
		}

		return clone;
	}

	private static class AllSuperCategories implements DynamicAttributeHandler<Collection<CategoryModel>, CategoryModel> {
		private static AllSuperCategories INSTANCE = new AllSuperCategories();

		@Override
		public Collection<CategoryModel> get(CategoryModel model) {
			List<CategoryModel> allCategories = new ArrayList<>();
			for (CategoryModel category : emptyIfNull(model.getSupercategories())) {
				allCategories.add(category);
				allCategories.addAll(get(category));
			}
			return allCategories;
		}

		@Override
		public void set(CategoryModel model, Collection<CategoryModel> categoryModels) {
			throw new IllegalArgumentException("Call to set not allowed!");
		}
	}
}
