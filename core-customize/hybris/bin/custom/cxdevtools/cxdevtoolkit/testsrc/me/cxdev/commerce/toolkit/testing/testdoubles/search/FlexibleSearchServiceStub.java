package me.cxdev.commerce.toolkit.testing.testdoubles.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.exceptions.AmbiguousIdentifierException;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.RelationQuery;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.search.TranslationResult;
import de.hybris.platform.servicelayer.search.impl.SearchResultImpl;

public class FlexibleSearchServiceStub implements FlexibleSearchService {
	private List<AbstractItemModel> models = new ArrayList<>();

	public FlexibleSearchServiceStub(AbstractItemModel... models) {
		if (models != null) {
			this.models = Arrays.asList(models);
		}
	}

	@Override
	public <T> T getModelByExample(T t) {
		return createUniqueResultBasedOnStubbedModels();
	}

	@Override
	public <T> List<T> getModelsByExample(T t) {
		return (List<T>) models;
	}

	@Override
	public <T> SearchResult<T> search(FlexibleSearchQuery flexibleSearchQuery) {
		return createSearchResultBasedOnStubbedModels();
	}

	@Override
	public <T> SearchResult<T> search(String s) {
		return createSearchResultBasedOnStubbedModels();
	}

	@Override
	public <T> SearchResult<T> search(String s, Map<String, ?> map) {
		return createSearchResultBasedOnStubbedModels();
	}

	@Override
	public <T> SearchResult<T> searchRelation(ItemModel itemModel, String s, int i, int i1) {
		return createSearchResultBasedOnStubbedModels();
	}

	@Override
	public <T> SearchResult<T> searchRelation(RelationQuery relationQuery) {
		return createSearchResultBasedOnStubbedModels();
	}

	@Override
	public <T> T searchUnique(FlexibleSearchQuery flexibleSearchQuery) {
		return createUniqueResultBasedOnStubbedModels();
	}

	@Override
	public TranslationResult translate(FlexibleSearchQuery flexibleSearchQuery) {
		throw new UnsupportedOperationException("Method not implemented for testing.");
	}

	private <T> T createUniqueResultBasedOnStubbedModels() {
		if (models.isEmpty()) {
			throw new ModelNotFoundException("Model not found!");
		} else if (models.size() > 1) {
			throw new AmbiguousIdentifierException("Model not unique!");
		}
		return (T) models.get(0);
	}

	private <T> SearchResultImpl<T> createSearchResultBasedOnStubbedModels() {
		return new SearchResultImpl<T>((List<T>) models, models.size(), models.size(), 0);
	}
}
