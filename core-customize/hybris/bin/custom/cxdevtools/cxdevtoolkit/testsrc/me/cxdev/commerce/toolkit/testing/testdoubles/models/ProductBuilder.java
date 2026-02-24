package me.cxdev.commerce.toolkit.testing.testdoubles.models;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.platform.core.model.product.ProductModel;

import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

public class ProductBuilder<ITEM extends ProductModel> {
	public static ProductBuilder<ProductModel> product(String code) {
		return product(code, ProductModel.class);
	}

	public static <T extends ProductModel> ProductBuilder<T> product(String code, Class<T> itemClass) {
		return new ProductBuilder<>(code, itemClass);
	}

	public static ProductBuilder<ProductModel> sampleProduct() {
		return product("sample")
				.name("Sample Category")
				.description("Description for Sample Category");
	}

	public static <T extends ProductModel> ProductBuilder<T> ofExistingProduct(T product) {
		return new ProductBuilder<>(product);
	}

	private ITEM product;
	private boolean useExisting;
	private List<CustomPropertyBuilder<ITEM>> customProperties = new ArrayList<>();

	private ProductBuilder(String code, Class<ITEM> itemClass) {
		this.product = InMemoryModelFactory.createTestableItemModel(itemClass);
		this.product.setCode(code);
		this.useExisting = false;
	}

	private ProductBuilder(ITEM product) {
		this.product = product;
		this.useExisting = true;
	}

	public ProductBuilder<ITEM> name(String name) {
		product.setName(name);
		return this;
	}

	public ProductBuilder<ITEM> names(Map<Locale, String> names) {
		for (Map.Entry<Locale, String> name : emptyIfNull(names).entrySet()) {
			product.setDescription(name.getValue(), name.getKey());
		}
		return this;
	}

	public ProductBuilder<ITEM> description(String description) {
		product.setDescription(description);
		return this;
	}

	public ProductBuilder<ITEM> descriptions(Map<Locale, String> descriptions) {
		for (Map.Entry<Locale, String> description : emptyIfNull(descriptions).entrySet()) {
			product.setDescription(description.getValue(), description.getKey());
		}
		return this;
	}

	public ProductBuilder<ITEM> valid(Date from, Date to) {
		product.setOnlineDate(from);
		product.setOfflineDate(to);
		return this;
	}

	public ProductBuilder<ITEM> customProperty(CustomPropertyBuilder<ITEM> custom) {
		customProperties.add(custom);
		return this;
	}

	public ITEM asModel() {
		ITEM clone = useExisting ? product : InMemoryModelFactory.copy(product);
		customProperties.stream().forEach(p -> p.setAttributeValueOnModel(clone));
		return clone;
	}
}
