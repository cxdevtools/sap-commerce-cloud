package tools.sapcx.commerce.search.resolver;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.exceptions.FieldValueProviderException;
import de.hybris.platform.solrfacetsearch.indexer.IndexerBatchContext;
import de.hybris.platform.solrfacetsearch.indexer.spi.InputDocument;
import de.hybris.platform.solrfacetsearch.provider.ValueResolver;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CxReferencedItemDelegatingValueResolver implements ValueResolver<ItemModel>, ApplicationContextAware {
	private static final String MISSING_VALUERESOLVERPARAMETER_EXCEPTION = "Missing required value resolver parameter '%s', please check configuration of indexed property '%s'!";
	private static final String MISSING_VALUERESOLVERBEAN_EXCEPTION = "Could not find delegated ValueResolver with ID '%s', please check configuration of indexed property '%s'!";

	private static final String REFERENCE_ATTRIBUTE_KEY = "referenceAttribute";
	private static final String DELEGATE_KEY = "delegate";

	private ApplicationContext applicationContext;
	private ModelService modelService;

	public CxReferencedItemDelegatingValueResolver(ModelService modelService) {
		this.modelService = modelService;
	}

	@Override
	public void resolve(InputDocument inputDocument, IndexerBatchContext indexerBatchContext, Collection<IndexedProperty> indexedProperties, ItemModel item)
			throws FieldValueProviderException {
		for (IndexedProperty indexedProperty : indexedProperties) {
			final ValueResolver<ItemModel> delegate = getDelegate(indexedProperty);
			final String referencedAttributeName = getRequiredValueProviderParameter(indexedProperty, REFERENCE_ATTRIBUTE_KEY);
			final Object referencedObject = modelService.getAttributeValue(item, referencedAttributeName);
			if (referencedObject instanceof ItemModel) {
				final ItemModel referencedItem = (ItemModel) referencedObject;
				resolveWithDelegate(inputDocument, indexerBatchContext, indexedProperty, referencedItem, delegate);
			} else if (referencedObject instanceof Collection) {
				for (Object element : (Collection) referencedObject) {
					if (element instanceof ItemModel) {
						resolveWithDelegate(inputDocument, indexerBatchContext, indexedProperty, (ItemModel) element, delegate);
					}
				}
			}
		}
	}

	private void resolveWithDelegate(InputDocument inputDocument, IndexerBatchContext indexerBatchContext, IndexedProperty indexedProperty, ItemModel referencedItem,
			ValueResolver<ItemModel> delegate) throws FieldValueProviderException {
		delegate.resolve(inputDocument, indexerBatchContext, List.of(indexedProperty), referencedItem);
	}

	private ValueResolver<ItemModel> getDelegate(IndexedProperty indexedProperty) throws FieldValueProviderException {
		final String delegateBeanId = getRequiredValueProviderParameter(indexedProperty, DELEGATE_KEY);
		try {
			return applicationContext.getBean(delegateBeanId, ValueResolver.class);
		} catch (BeansException e) {
			final String msg = String.format(MISSING_VALUERESOLVERBEAN_EXCEPTION, delegateBeanId, indexedProperty.getName());
			throw new FieldValueProviderException(msg, e);
		}
	}

	private String getRequiredValueProviderParameter(final IndexedProperty indexedProperty, final String key) throws FieldValueProviderException {
		final Map<String, String> valueProviderParameters = MapUtils.emptyIfNull(indexedProperty.getValueProviderParameters());
		final String parameterValue = valueProviderParameters.get(key);
		return Optional.ofNullable(parameterValue)
				.orElseThrow(() -> {
					final String msg = String.format(MISSING_VALUERESOLVERPARAMETER_EXCEPTION, DELEGATE_KEY, indexedProperty.getName());
					return new FieldValueProviderException(msg);
				});
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
