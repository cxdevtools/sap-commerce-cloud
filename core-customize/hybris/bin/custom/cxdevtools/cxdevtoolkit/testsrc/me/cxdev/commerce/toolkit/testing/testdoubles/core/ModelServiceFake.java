package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import static me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.createTestableItemModel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.exceptions.ModelInitializationException;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.model.ModelService;

public class ModelServiceFake implements ModelService {
	@Override
	public void attach(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void detach(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void detach(PK pk) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void detachAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T clone(T t) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T clone(T t, ModelCloningContext modelCloningContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass, ModelCloningContext modelCloningContext) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T create(Class aClass) {
		return (T) createTestableItemModel(aClass);
	}

	@Override
	public <T> T create(String s) {
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
	public void refresh(Object o) {
		// Do nothing for now
	}

	@Override
	public void save(Object o) throws ModelSavingException {
	}

	@Override
	public void saveAll(Collection<?> collection) throws ModelSavingException {
	}

	@Override
	public void saveAll(Object... objects) throws ModelSavingException {
	}

	@Override
	public void saveAll() throws ModelSavingException {
	}

	@Override
	public boolean isUniqueConstraintErrorAsRootCause(Exception e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(Object o) throws ModelRemovalException {
	}

	@Override
	public void removeAll(Collection<?> collection) throws ModelRemovalException {
	}

	@Override
	public void removeAll(Object... objects) throws ModelRemovalException {
	}

	@Override
	public void remove(PK pk) throws ModelRemovalException {
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
	public void initDefaults(Object o) throws ModelInitializationException {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isModified(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isNew(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRemoved(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAttached(Object o) {
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
