package me.cxdev.commerce.backoffice.sync;

import java.util.*;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.model.visitor.ItemVisitor;
import de.hybris.platform.servicelayer.type.TypeService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.VisibleForTesting;

/**
 * Generic implementation of {@link ItemVisitor}. This class shall avoid that new {@link ItemVisitor} classes need to
 * be introduced for each and every relation that influences the sync status of an {@link ItemModel}. Instead, one can
 * configure the relation by introducing a platform property with the following convention:
 *
 * cxdevbackoffice.sync.relateditems.<ItemType>=<AttributeId>,<AttributeId>,...
 *
 * For example, the following relations would be valid:
 * cxdevbackoffice.sync.relateditems.Product=mymedia,prospect,reference
 * cxdevbackoffice.sync.relateditems.Category=heroProduct
 * ...
 */
public class GenericItemSyncRelatedItemsVisitor implements ItemVisitor<ItemModel> {
	@VisibleForTesting
	protected static final String CXBACKOFFICE_SYNC_RELATEDITEMS = "cxdevbackoffice.sync.relateditems.";

	private ModelService modelService;
	private Map<String, List<String>> attributes = new HashMap<>();
	private TypeService typeService;
	private Properties hybrisProperties;

	@Override
	public List<ItemModel> visit(ItemModel itemModel, List<ItemModel> list, Map<String, Object> map) {
		ArrayList<String> typeCodes = getTypeCodesForItemModel(itemModel);

		return typeCodes.stream()
				.map(code -> attributes.get(code))
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(attr -> this.getAttributeAsList(itemModel, attr))
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.toList();
	}

	private ArrayList<String> getTypeCodesForItemModel(ItemModel itemModel) {
		ComposedTypeModel type = typeService.getComposedTypeForClass(itemModel.getClass());
		List<String> superTypes = CollectionUtils.emptyIfNull(type.getAllSuperTypes()).stream()
				.map(ComposedTypeModel::getCode)
				.toList();
		ArrayList<String> typeCodes = new ArrayList<>(superTypes);
		typeCodes.add(type.getCode());
		return typeCodes;
	}

	private Collection<ItemModel> getAttributeAsList(ItemModel model, String attribute) {
		Object rawAttribute = modelService.getAttributeValue(model, attribute);
		if (rawAttribute instanceof ItemModel) {
			return Collections.singletonList((ItemModel) rawAttribute);
		}
		if (rawAttribute instanceof Collection) {
			return (Collection<ItemModel>) rawAttribute;
		}
		return Collections.emptyList();
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	public void setTypeService(TypeService typeService) {
		this.typeService = typeService;
	}

	public void setHybrisProperties(Properties hybrisProperties) {
		this.hybrisProperties = hybrisProperties;
	}

	public void initAttributes() {
		attributes.clear();
		hybrisProperties.stringPropertyNames().stream()
				.filter(propertyName -> propertyName.startsWith(CXBACKOFFICE_SYNC_RELATEDITEMS))
				.map(propertyName -> StringUtils.substringAfterLast(propertyName, "."))
				.forEach(type -> attributes.put(type, getSplittedValues(hybrisProperties, type)));
	}

	private List<String> getSplittedValues(Properties properties, String name) {
		return Arrays.asList(properties.getProperty(CXBACKOFFICE_SYNC_RELATEDITEMS + name, "").split(","));
	}
}
