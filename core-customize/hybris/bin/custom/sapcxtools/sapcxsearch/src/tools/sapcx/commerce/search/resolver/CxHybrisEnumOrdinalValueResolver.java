package tools.sapcx.commerce.search.resolver;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.HashMap;
import java.util.Map;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.impl.AbstractValueResolver;

import org.apache.commons.lang3.StringUtils;

public class CxHybrisEnumOrdinalValueResolver extends AbstractValueResolver<ItemModel, Object, Object> {
	private static final int DEFAULT_FALLBACK_VALUE = 9999;

	private Map<String, Integer> enumValuesOrdinalMap = new HashMap<>();
	private int fallbackValue;

	public CxHybrisEnumOrdinalValueResolver(Map<String, Integer> enumValuesOrdinalMap) {
		this(enumValuesOrdinalMap, DEFAULT_FALLBACK_VALUE);
	}

	public CxHybrisEnumOrdinalValueResolver(Map<String, Integer> enumValuesOrdinalMap, int fallbackValue) {
		this.enumValuesOrdinalMap.putAll(emptyIfNull(enumValuesOrdinalMap));
		this.fallbackValue = fallbackValue;
	}

	@Override
	protected void addFieldValues(InputDocument inputDocument, IndexerBatchContext indexerContext, IndexedProperty indexedProperty, ItemModel item,
			ValueResolverContext<Object, Object> resolverContext) throws FieldValueProviderException {
		String propertyName = getPropertyName(indexedProperty);
		HybrisEnumValue enumValue = item.getProperty(propertyName);
		int enumOrdinal = mapEnumValueToOrdinal(enumValue);
		inputDocument.addField(indexedProperty, enumOrdinal);
	}

	private String getPropertyName(IndexedProperty indexedProperty) {
		String providerParameter = indexedProperty.getValueProviderParameter();
		String indexedPropertyName = indexedProperty.getName();
		return StringUtils.defaultIfBlank(providerParameter, indexedPropertyName);
	}

	private int mapEnumValueToOrdinal(HybrisEnumValue enumValue) {
		if (enumValue == null) {
			return fallbackValue;
		}

		String code = enumValue.getCode();
		return enumValuesOrdinalMap.getOrDefault(code, fallbackValue);
	}
}
