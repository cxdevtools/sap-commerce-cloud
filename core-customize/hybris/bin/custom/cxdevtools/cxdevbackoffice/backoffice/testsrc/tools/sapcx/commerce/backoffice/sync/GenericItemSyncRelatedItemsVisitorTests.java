package me.cxdev.commerce.backoffice.sync;

import static org.hamcrest.Matchers.*;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.variants.model.VariantProductModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import me.cxdev.commerce.toolkit.testing.testdoubles.core.DummyTypeService;
import me.cxdev.commerce.toolkit.testing.testdoubles.core.InMemoryModelService;

@UnitTest
public class GenericItemSyncRelatedItemsVisitorTests {
	private DummyTypeService typeService;
	private InMemoryModelService modelService;
	private Properties hybrisProperties = new Properties();
	private GenericItemSyncRelatedItemsVisitor itemVisitor;

	@Before
	public void setup() {
		modelService = new InMemoryModelService();
		typeService = new DummyTypeService();

		itemVisitor = new GenericItemSyncRelatedItemsVisitor();
		itemVisitor.setTypeService(typeService);
		itemVisitor.setModelService(modelService);
	}

	private void setProperties(Map<String, List<String>> attributes) {
		hybrisProperties = new Properties();
		attributes.keySet()
				.forEach(type -> hybrisProperties.put(GenericItemSyncRelatedItemsVisitor.CXBACKOFFICE_SYNC_RELATEDITEMS + type,
						attributes.get(type).stream().collect(Collectors.joining(","))));
		itemVisitor.setHybrisProperties(hybrisProperties);
		itemVisitor.initAttributes();
	}

	@Test
	public void visit_type_not_configured() {
		MediaModel item = modelService.create(MediaModel.class);
		ComposedTypeModel mediaType = modelService.create(ComposedTypeModel.class);
		mediaType.setCode("Media");
		typeService.type = mediaType;
		modelService.saveAll();

		setProperties(ImmutableMap.of(
				"Category", Arrays.asList("products", "medias"),
				"Product", Collections.singletonList("images"),
				"VariantProduct", Collections.singletonList("baseProduct")));

		List<ItemModel> visited = itemVisitor.visit(item, Collections.emptyList(), Collections.emptyMap());

		Assert.assertThat(visited, hasSize(0));
	}

	@Test
	public void visit_type_wrong_config() {
		MediaModel item = modelService.create(MediaModel.class);
		ComposedTypeModel mediaType = modelService.create(ComposedTypeModel.class);
		mediaType.setCode("Media");
		typeService.type = mediaType;
		modelService.saveAll();

		setProperties(ImmutableMap.of("sdfsdg", Collections.singletonList("llkjlkj")));

		List<ItemModel> visited = itemVisitor.visit(item, Collections.emptyList(), Collections.emptyMap());

		Assert.assertThat(visited, hasSize(0));
	}

	@Test
	public void visit_type_string_attribute() {
		MediaModel item = modelService.create(MediaModel.class);
		modelService.setAttributeValue(item, "code", "mediacode");
		ComposedTypeModel mediaType = modelService.create(ComposedTypeModel.class);
		mediaType.setCode("Media");
		typeService.type = mediaType;
		modelService.saveAll();

		setProperties(ImmutableMap.of("Media", Collections.singletonList("code")));

		List<ItemModel> visited = itemVisitor.visit(item, Collections.emptyList(), Collections.emptyMap());

		Assert.assertThat(visited, hasSize(0));
	}

	@Test
	public void visit_type_configured() {
		CategoryModel item = modelService.create(CategoryModel.class);
		List<MediaModel> mediaList = Arrays.asList(modelService.create(MediaModel.class), modelService.create(MediaModel.class));
		modelService.setAttributeValue(item, "medias", mediaList);
		modelService.setAttributeValue(item, "products", Collections.singletonList(modelService.create(ProductModel.class)));
		ComposedTypeModel categoryType = modelService.create(ComposedTypeModel.class);
		categoryType.setCode("Category");
		typeService.type = categoryType;
		modelService.saveAll();

		setProperties(ImmutableMap.of(
				"Category", Arrays.asList("products", "medias"),
				"Product", Collections.singletonList("images"),
				"VariantProduct", Collections.singletonList("baseProduct")));

		List<ItemModel> visited = itemVisitor.visit(item, Collections.emptyList(), Collections.emptyMap());

		Assert.assertThat(visited, hasSize(3));
		Assert.assertThat(visited, hasItem(isA(ProductModel.class)));
		Assert.assertThat(visited, hasItem(isA(MediaModel.class)));
	}

	@Test
	public void visit_type_parent_configured() {
		VariantProductModel item = modelService.create(VariantProductModel.class);
		MediaModel media1 = modelService.create(MediaModel.class);
		MediaModel media2 = modelService.create(MediaModel.class);
		List<MediaModel> mediaList = Arrays.asList(media1, media2);
		modelService.setAttributeValue(item, "images", mediaList);
		ProductModel baseProduct = modelService.create(ProductModel.class);
		modelService.setAttributeValue(item, "baseProduct", baseProduct);
		ComposedTypeModel variantType = modelService.create(ComposedTypeModel.class);
		variantType.setCode("VariantProduct");
		ComposedTypeModel productType = modelService.create(ComposedTypeModel.class);
		productType.setCode("Product");
		ComposedTypeModel itemType = modelService.create(ComposedTypeModel.class);
		itemType.setCode("Item");
		variantType.setProperty(ComposedTypeModel.ALLSUPERTYPES, Arrays.asList(productType, itemType));
		typeService.type = variantType;
		modelService.saveAll();

		setProperties(ImmutableMap.of(
				"Category", Arrays.asList("products", "medias"),
				"Product", Collections.singletonList("images"),
				"VariantProduct", Collections.singletonList("baseProduct")));

		List<ItemModel> visited = itemVisitor.visit(item, Collections.emptyList(), Collections.emptyMap());

		Assert.assertThat(visited, hasSize(3));
		Assert.assertThat(visited, containsInAnyOrder(media1, media2, baseProduct));
	}
}
