package tools.sapcx.commerce.search.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;
import de.hybris.platform.variants.model.VariantProductModel;

public class CxInheritanceValueProvider implements FieldValueProvider {
	private FieldNameProvider fieldNameProvider;

	@Override
	public Collection<FieldValue> getFieldValues(IndexConfig indexConfig, IndexedProperty indexedProperty, Object o) throws FieldValueProviderException {
		if (o instanceof ProductModel) {
			Object value = getInheritedValue(indexedProperty, (ProductModel) o);
			if (value != null) {
				return createFieldValues(indexedProperty, value);
			}
		}
		return Collections.EMPTY_LIST;
	}

	protected List<FieldValue> createFieldValues(IndexedProperty indexedProperty, Object value) {
		List<FieldValue> fieldValues = new ArrayList<>();
		final Collection<String> fieldNames = fieldNameProvider.getFieldNames(indexedProperty, null);
		for (final String fieldName : fieldNames) {
			fieldValues.add(new FieldValue(fieldName, value));
		}
		return fieldValues;
	}

	protected Object getInheritedValue(IndexedProperty indexedProperty, ProductModel product) {
		String propertyName = indexedProperty.getName();
		Object value = product.getProperty(propertyName);
		if (value != null) {
			return value;
		}

		if (product instanceof VariantProductModel) {
			return getInheritedValue(indexedProperty, ((VariantProductModel) product).getBaseProduct());
		}
		return null;
	}

	public void setFieldNameProvider(FieldNameProvider fieldNameProvider) {
		this.fieldNameProvider = fieldNameProvider;
	}
}
