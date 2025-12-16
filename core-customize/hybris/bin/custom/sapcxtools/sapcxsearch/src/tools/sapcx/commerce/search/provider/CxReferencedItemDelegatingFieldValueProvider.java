package tools.sapcx.commerce.search.provider;

import static org.apache.commons.collections4.MapUtils.emptyIfNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.provider.FieldValue;
import de.hybris.platform.solrfacetsearch.provider.FieldValueProvider;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CxReferencedItemDelegatingFieldValueProvider implements FieldValueProvider, ApplicationContextAware {
	private static final String MISSING_VALUEPROVIDERPARAMETER_EXCEPTION = "Missing required value provider parameter '%s', please check configuration of indexed property '%s'!";
	private static final String MISSING_VALUEPROVIDERBEAN_EXCEPTION = "Could not find delegated FieldValueProvider with ID '%s', please check configuration of indexed property '%s'!";

	private static final String REFERENCE_ATTRIBUTE_KEY = "referenceAttribute";
	private static final String DELEGATE_KEY = "delegate";

	private ApplicationContext applicationContext;
	private ModelService modelService;

	public CxReferencedItemDelegatingFieldValueProvider(ModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public Collection<FieldValue> getFieldValues(IndexConfig indexConfig, IndexedProperty indexedProperty, Object object) throws FieldValueProviderException {
		final FieldValueProvider delegate = getDelegate(indexedProperty);
		final String referencedAttributeName = getRequiredValueProviderParameter(indexedProperty, REFERENCE_ATTRIBUTE_KEY);

		if (object instanceof ItemModel) {
			final ItemModel item = (ItemModel) object;
			final Object referencedObject = modelService.getAttributeValue(item, referencedAttributeName);
			if (referencedObject instanceof ItemModel) {
				final ItemModel referencedItem = (ItemModel) referencedObject;
				return getFieldValuesFromDelegate(indexConfig, indexedProperty, referencedItem, delegate);
			} else if (referencedObject instanceof Collection<?>) {
				final Collection<?> elements = (Collection<?>) referencedObject;
				final Collection<FieldValue> fieldValues = new ArrayList<>();
				for (Object element : elements) {
					fieldValues.addAll(getFieldValuesFromDelegate(indexConfig, indexedProperty, element, delegate));
				}
				return fieldValues;
			}
		}

		return List.of();
	}

	private Collection<FieldValue> getFieldValuesFromDelegate(IndexConfig indexConfig, IndexedProperty indexedProperty, Object referencedObject, FieldValueProvider delegate)
			throws FieldValueProviderException {
		return delegate.getFieldValues(indexConfig, indexedProperty, referencedObject);
	}

	private FieldValueProvider getDelegate(IndexedProperty indexedProperty) throws FieldValueProviderException {
		final String delegateBeanId = getRequiredValueProviderParameter(indexedProperty, DELEGATE_KEY);
		try {
			return applicationContext.getBean(delegateBeanId, FieldValueProvider.class);
		} catch (BeansException e) {
			final String msg = String.format(MISSING_VALUEPROVIDERBEAN_EXCEPTION, delegateBeanId, indexedProperty.getName());
			throw new FieldValueProviderException(msg, e);
		}
	}

	private String getRequiredValueProviderParameter(final IndexedProperty indexedProperty, final String key) throws FieldValueProviderException {
		final Map<String, String> valueProviderParameters = emptyIfNull(indexedProperty.getValueProviderParameters());
		final String parameterValue = valueProviderParameters.get(key);
		return Optional.ofNullable(parameterValue)
				.orElseThrow(() -> {
					final String msg = String.format(MISSING_VALUEPROVIDERPARAMETER_EXCEPTION, DELEGATE_KEY, indexedProperty.getName());
					return new FieldValueProviderException(msg);
				});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
