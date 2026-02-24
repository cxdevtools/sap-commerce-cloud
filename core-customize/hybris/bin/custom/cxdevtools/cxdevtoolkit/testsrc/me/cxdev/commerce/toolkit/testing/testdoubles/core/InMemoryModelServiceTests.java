package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.servicelayer.model.ModelService;

import org.junit.Test;

@UnitTest
public class InMemoryModelServiceTests {
	private static final Date NOW = new Date();

	private ModelService modelService = new InMemoryModelService();

	@Test
	public void verifyAttributesAreMarkedDirtyOnChange() {
		ProductModel product = modelService.create(ProductModel.class);

		product.setCode("code");
		product.setOnlineDate(NOW);
		product.setName("name", Locale.ENGLISH);

		assertThat(product.getCode()).isEqualTo("code");
		assertThat(product.getOnlineDate()).isEqualTo(NOW);
		assertThat(product.getName(Locale.ENGLISH)).isEqualTo("name");

		assertThat(product.getItemModelContext().getDirtyAttributes()).containsOnly(ProductModel.CODE, ProductModel.ONLINEDATE, ProductModel.NAME);

		Map<Locale, Set<String>> dirtyLocalizedAttributes = product.getItemModelContext().getDirtyLocalizedAttributes();
		assertThat(dirtyLocalizedAttributes).containsKeys(Locale.ENGLISH);
		assertThat(dirtyLocalizedAttributes.get(Locale.ENGLISH)).containsOnly(ProductModel.NAME);

		assertThat(modelService.isNew(product)).isTrue();
		assertThat(modelService.isAttached(product)).isTrue();
		assertThat(modelService.isModified(product)).isTrue();
		assertThat(modelService.isUpToDate(product)).isFalse();
		assertThat(modelService.isRemoved(product)).isFalse();
	}

	@Test
	public void verifyAttributesAreNoLongerMarkedDirtyAfterSave() {
		ProductModel product = modelService.create(ProductModel.class);

		product.setCode("code");
		product.setOnlineDate(NOW);
		product.setName("name", Locale.ENGLISH);

		modelService.save(product);

		assertThat(product.getCode()).isEqualTo("code");
		assertThat(product.getOnlineDate()).isEqualTo(NOW);
		assertThat(product.getName(Locale.ENGLISH)).isEqualTo("name");

		assertThat(product.getItemModelContext().getDirtyAttributes()).isEmpty();
		assertThat(product.getItemModelContext().getDirtyLocalizedAttributes()).isEmpty();

		assertThat(modelService.isNew(product)).isFalse();
		assertThat(modelService.isAttached(product)).isTrue();
		assertThat(modelService.isModified(product)).isFalse();
		assertThat(modelService.isUpToDate(product)).isTrue();
		assertThat(modelService.isRemoved(product)).isFalse();
	}

	@Test
	public void verifyAttributesAreMarkedDirtyAfterSaveAndModify() {
		ProductModel product = modelService.create(ProductModel.class);

		product.setCode("code");
		product.setOnlineDate(NOW);
		product.setName("name", Locale.ENGLISH);

		modelService.save(product);

		product.setCode("new code");

		assertThat(product.getCode()).isEqualTo("new code");
		assertThat(product.getOnlineDate()).isEqualTo(NOW);
		assertThat(product.getName(Locale.ENGLISH)).isEqualTo("name");

		assertThat(product.getItemModelContext().getDirtyAttributes()).containsOnly(ProductModel.CODE);
		assertThat(product.getItemModelContext().getDirtyLocalizedAttributes()).isEmpty();

		assertThat(modelService.isNew(product)).isFalse();
		assertThat(modelService.isAttached(product)).isTrue();
		assertThat(modelService.isModified(product)).isTrue();
		assertThat(modelService.isUpToDate(product)).isFalse();
		assertThat(modelService.isRemoved(product)).isFalse();
	}

	@Test
	public void verifyAttributesAreReloaded() {
		ProductModel product = modelService.create(ProductModel.class);

		product.setCode("code");
		product.setOnlineDate(NOW);
		product.setName("name", Locale.ENGLISH);

		modelService.save(product);

		product.setCode("new code");

		modelService.refresh(product);

		assertThat(product.getCode()).isEqualTo("code");
		assertThat(product.getOnlineDate()).isEqualTo(NOW);
		assertThat(product.getName(Locale.ENGLISH)).isEqualTo("name");

		assertThat(product.getItemModelContext().getDirtyAttributes()).isEmpty();
		assertThat(product.getItemModelContext().getDirtyLocalizedAttributes()).isEmpty();

		assertThat(modelService.isNew(product)).isFalse();
		assertThat(modelService.isAttached(product)).isTrue();
		assertThat(modelService.isModified(product)).isFalse();
		assertThat(modelService.isUpToDate(product)).isTrue();
		assertThat(modelService.isRemoved(product)).isFalse();
	}

	@Test
	public void verifyAllAttachedModelsAreSaved() {
		ProductModel product = modelService.create(ProductModel.class);
		CategoryModel category = modelService.create(CategoryModel.class);

		product.setCode("product");
		category.setCode("category");
		modelService.saveAll();

		assertThat(modelService.isNew(product)).isFalse();
		assertThat(modelService.isAttached(product)).isTrue();
		assertThat(modelService.isModified(product)).isFalse();
		assertThat(modelService.isUpToDate(product)).isTrue();
		assertThat(modelService.isRemoved(product)).isFalse();

		assertThat(modelService.isNew(category)).isFalse();
		assertThat(modelService.isAttached(category)).isTrue();
		assertThat(modelService.isModified(category)).isFalse();
		assertThat(modelService.isUpToDate(category)).isTrue();
		assertThat(modelService.isRemoved(category)).isFalse();
	}

	@Test
	public void verifyRemovedItemsAreDetached() {
		ProductModel product = modelService.create(ProductModel.class);
		CategoryModel category = modelService.create(CategoryModel.class);

		product.setCode("product");
		category.setCode("category");
		modelService.saveAll();
		modelService.remove(category);

		assertThat(modelService.isNew(product)).isFalse();
		assertThat(modelService.isAttached(product)).isTrue();
		assertThat(modelService.isModified(product)).isFalse();
		assertThat(modelService.isUpToDate(product)).isTrue();
		assertThat(modelService.isRemoved(product)).isFalse();

		assertThat(modelService.isNew(category)).isFalse();
		assertThat(modelService.isAttached(category)).isFalse();
		assertThat(modelService.isModified(category)).isFalse();
		assertThat(modelService.isUpToDate(category)).isTrue();
		assertThat(modelService.isRemoved(category)).isTrue();
	}

	@Test
	public void verifyCloneWorksAndClonedItemIsAttached() {
		ProductModel product = modelService.create(ProductModel.class);
		product.setCode("product");

		ProductModel clone = modelService.clone(product);

		assertThat(modelService.isNew(clone)).isTrue();
		assertThat(modelService.isAttached(clone)).isTrue();
		assertThat(modelService.isModified(clone)).isTrue();
		assertThat(modelService.isUpToDate(clone)).isFalse();
		assertThat(modelService.isRemoved(clone)).isFalse();
	}
}
