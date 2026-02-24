package me.cxdev.commerce.toolkit.testing.testdoubles.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.internal.dao.SortParameters;

import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

public class GenericDaoFake<T extends ItemModel> implements GenericDao<T> {
	public static <T extends RuntimeException> GenericDao throwingException(Class<T> exceptionToThrow) {
		return new GenericDaoFakeThrowingException<T>(exceptionToThrow);
	}

	public static <T extends ItemModel> GenericDaoFake<T> empty() {
		return new GenericDaoFake<T>(List.of());
	}

	public static <T extends ItemModel> GenericDao<T> ofCount(int count, Class<T> modelClass) {
		List<T> models = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			models.add(InMemoryModelFactory.createTestableItemModel(modelClass));
		}
		return forModels(models);
	}

	public static <T extends ItemModel> GenericDaoFake<T> forModel(T model) {
		return new GenericDaoFake<T>(List.of(model));
	}

	public static <T extends ItemModel> GenericDaoFake<T> forModels(List<T> models) {
		return new GenericDaoFake<T>(models);
	}

	public int count;
	public Map<String, ?> map;

	public SortParameters sortParameters;

	private List<T> models;

	public GenericDaoFake(List<T> models) {
		this.models = new ArrayList<>(models);
	}

	@Override
	public List<T> find() {
		return this.models;
	}

	@Override
	public List<T> find(Map<String, ?> map) {
		this.map = map;
		return models.stream().filter(model -> matchesCriteriaMap(map, model)).collect(Collectors.toList());

	}

	@Override
	public List<T> find(SortParameters sortParameters) {
		this.sortParameters = sortParameters;
		return this.models;
	}

	@Override
	public List<T> find(Map<String, ?> map, SortParameters sortParameters) {
		this.map = map;
		this.sortParameters = sortParameters;
		return models.stream().filter(model -> matchesCriteriaMap(map, model)).collect(Collectors.toList());
	}

	@Override
	public List<T> find(Map<String, ?> map, SortParameters sortParameters, int count) {
		this.map = map;
		this.sortParameters = sortParameters;
		this.count = count;
		return models.stream().filter(model -> matchesCriteriaMap(map, model)).limit(count).collect(Collectors.toList());

	}

	private boolean matchesCriteriaMap(Map<String, ?> map, T model) {
		for (String key : map.keySet()) {
			if (!model.getProperty(key).equals(map.get(key))) {
				return false;
			}
		}
		return true;
	}
}
