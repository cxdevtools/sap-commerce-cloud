package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.*;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.exceptions.ModelInitializationException;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.model.AbstractItemModel;
import de.hybris.platform.servicelayer.model.ModelService;

import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;

public class InMemoryModelService implements ModelService {
	private List<Object> removedItemModels = new ArrayList<>();
	private List<Object> attachedItemModels = new ArrayList<>();

	@Override
	public void attach(Object o) {
		if (!isAttached(o)) {
			attachedItemModels.add(o);
		}
	}

	@Override
	public void detach(Object o) {
		if (isAttached(o)) {
			attachedItemModels.remove(o);
		}
	}

	@Override
	public void detach(PK pk) {
		attachedItemModels.stream()
				.filter(AbstractItemModel.class::isInstance)
				.map(AbstractItemModel.class::cast)
				.filter(item -> pk.equals(item.getPk()))
				.forEach(this::detach);
	}

	@Override
	public void detachAll() {
		attachedItemModels.clear();
	}

	@Override
	public <T> T clone(T t) {
		T copy = (T) InMemoryModelFactory.copy((ItemModel) t);
		attach(copy);
		return copy;
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass) {
		return (T) clone(o);
	}

	@Override
	public <T> T clone(T t, ModelCloningContext modelCloningContext) {
		return clone(t);
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass, ModelCloningContext modelCloningContext) {
		return clone(o, aClass);
	}

	@Override
	public <T> T create(Class aClass) {
		T item = (T) InMemoryModelFactory.createTestableItemModel(aClass);
		attach(item);
		return item;
	}

	@Override
	public void refresh(Object o) {
		InMemoryModelFactory.getContextAccessor(o).refresh();
	}

	@Override
	public void save(Object o) throws ModelSavingException {
		attach(o);
		InMemoryModelFactory.getContextAccessor(o).save();
	}

	@Override
	public void saveAll(Collection<?> collection) throws ModelSavingException {
		emptyIfNull(collection).stream().forEach(this::save);
	}

	@Override
	public void saveAll(Object... objects) throws ModelSavingException {
		Arrays.asList(objects).stream().forEach(this::save);
	}

	@Override
	public void saveAll() throws ModelSavingException {
		saveAll(attachedItemModels);
	}

	@Override
	public boolean isUniqueConstraintErrorAsRootCause(Exception e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Object o) throws ModelRemovalException {
		if (!isRemoved(o)) {
			removedItemModels.add(o);
		}
		detach(o);
	}

	@Override
	public void removeAll(Collection<?> collection) throws ModelRemovalException {
		emptyIfNull(collection).stream().forEach(this::remove);
	}

	@Override
	public void removeAll(Object... objects) throws ModelRemovalException {
		Arrays.asList(objects).stream().forEach(this::remove);
	}

	@Override
	public void remove(PK pk) throws ModelRemovalException {
		attachedItemModels.stream()
				.filter(AbstractItemModel.class::isInstance)
				.map(AbstractItemModel.class::cast)
				.filter(item -> pk.equals(item.getPk()))
				.forEach(this::remove);
	}

	@Override
	public <T> T getAttributeValue(Object o, String s) {
		return ((ItemModel) o).getProperty(s);
	}

	@Override
	public <T> T getAttributeValue(Object o, String s, Locale locale) {
		return ((ItemModel) o).getProperty(s, locale);
	}

	@Override
	public <T> Map<Locale, T> getAttributeValues(Object o, String s, Locale... locales) {
		Map<Locale, T> values = new HashMap<>(locales.length);
		for (Locale locale : locales) {
			values.put(locale, ((ItemModel) o).getProperty(s, locale));
		}
		return values;
	}

	@Override
	public void setAttributeValue(Object o, String s, Object o1) {
		((ItemModel) o).setProperty(s, o1);
	}

	@Override
	public <T> void setAttributeValue(Object o, String s, Map<Locale, T> map) {
		for (Map.Entry<Locale, T> entry : map.entrySet()) {
			((ItemModel) o).setProperty(s, entry.getKey(), entry.getValue());
		}
	}

	@Override
	public boolean isUpToDate(Object o) {
		return !isNew(o) && !isModified(o);
	}

	@Override
	public boolean isModified(Object o) {
		return InMemoryModelFactory.getContextAccessor(o).isDirty();
	}

	@Override
	public boolean isNew(Object o) {
		return InMemoryModelFactory.getContextAccessor(o).isNew();
	}

	@Override
	public boolean isRemoved(Object o) {
		return removedItemModels.contains(o);
	}

	@Override
	public boolean isAttached(Object o) {
		return attachedItemModels.contains(o);
	}

	@Override
	public <T> T create(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void initDefaults(Object o) throws ModelInitializationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getModelType(Class aClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class getModelTypeClass(Class aClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getModelType(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T toModelLayer(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T toPersistenceLayer(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T get(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T get(Object o, String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T get(PK pk) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Collection> T getAll(Collection<?> collection, T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Collection> T getAll(Collection<?> collection, T t, String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getSource(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends Collection> T getAllSources(Collection<?> collection, T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSourceAttached(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void enableTransactions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void disableTransactions() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearTransactionsSettings() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getByExample(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void lock(PK pk) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void lock(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T getWithLock(Object o) {
		throw new UnsupportedOperationException();
	}
}
