package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.servicelayer.exceptions.ModelInitializationException;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.model.ModelService;

public class ModelServiceSpy implements ModelService {
	public Class<?> createWasCalledWithClass;
	public List<Object> savedItems = new ArrayList<>();
	public List<Object> removedItems = new ArrayList<>();
	public Map<Object, Map<String, Object>> attributeValues = new HashMap<>();
	public Object isRemovedInput = null;
	public boolean isRemovedOutput = false;

	private ItemModel itemModel;

	public ModelServiceSpy(ItemModel itemModel) {
		this.itemModel = itemModel;
	}

	@Override
	public void attach(Object o) {
	}

	@Override
	public void detach(Object o) {
	}

	@Override
	public void detach(PK pk) {
	}

	@Override
	public void detachAll() {
	}

	@Override
	public <T> T clone(T t) {
		return null;
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass) {
		return null;
	}

	@Override
	public <T> T clone(T t, ModelCloningContext modelCloningContext) {
		return null;
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass, ModelCloningContext modelCloningContext) {
		return null;
	}

	@Override
	public <T> T create(Class aClass) {
		createWasCalledWithClass = aClass;
		return (T) itemModel;
	}

	@Override
	public <T> T create(String s) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T get(Object o) {
		return null;
	}

	@Override
	public <T> T get(Object o, String s) {
		return null;
	}

	@Override
	public <T> T get(PK pk) {
		return null;
	}

	@Override
	public <T extends Collection> T getAll(Collection<?> collection, T t) {
		return null;
	}

	@Override
	public <T extends Collection> T getAll(Collection<?> collection, T t, String s) {
		return null;
	}

	@Override
	public void refresh(Object o) {

	}

	@Override
	public void save(Object o) throws ModelSavingException {
		savedItems.add(o);
	}

	@Override
	public void saveAll(Collection<? extends Object> collection) throws ModelSavingException {
		savedItems.addAll(collection);
	}

	@Override
	public void saveAll(Object... objects) throws ModelSavingException {
		savedItems.addAll(Arrays.asList(objects));
	}

	@Override
	public void saveAll() throws ModelSavingException {

	}

	@Override
	public boolean isUniqueConstraintErrorAsRootCause(Exception e) {
		throw new UnsupportedOperationException("isUniqueConstraintErrorAsRootCause not supported by ModelServiceSpy", e);
	}

	@Override
	public void remove(Object o) throws ModelRemovalException {
		removedItems.add(o);
	}

	@Override
	public void removeAll(Collection<? extends Object> collection) throws ModelRemovalException {
		removedItems.addAll(collection);
	}

	@Override
	public void removeAll(Object... objects) throws ModelRemovalException {
		removedItems.addAll(Arrays.asList(objects));
	}

	@Override
	public void remove(PK pk) throws ModelRemovalException {

	}

	@Override
	public <T> T getSource(Object o) {
		return null;
	}

	@Override
	public <T extends Collection> T getAllSources(Collection<?> collection, T t) {
		return null;
	}

	@Override
	public String getModelType(Class aClass) {
		return null;
	}

	@Override
	public Class getModelTypeClass(Class aClass) {
		return null;
	}

	@Override
	public String getModelType(Object o) {
		return null;
	}

	@Override
	public <T> T toModelLayer(Object o) {
		return null;
	}

	@Override
	public <T> T toPersistenceLayer(Object o) {
		return null;
	}

	@Override
	public void initDefaults(Object o) throws ModelInitializationException {

	}

	@Override
	public <T> T getAttributeValue(Object o, String s) {
		return null;
	}

	@Override
	public <T> T getAttributeValue(Object o, String s, Locale locale) {
		return null;
	}

	@Override
	public <T> Map<Locale, T> getAttributeValues(Object o, String s, Locale... locales) {
		return null;
	}

	@Override
	public void setAttributeValue(Object item, String attribute, Object value) {
		if (!attributeValues.containsKey(item)) {
			attributeValues.put(item, new HashMap<>());
		}
		attributeValues.get(item).put(attribute, value);
	}

	@Override
	public <T> void setAttributeValue(Object o, String s, Map<Locale, T> map) {

	}

	@Override
	public boolean isUpToDate(Object o) {
		return false;
	}

	@Override
	public boolean isModified(Object o) {
		return false;
	}

	@Override
	public boolean isNew(Object o) {
		return false;
	}

	@Override
	public boolean isRemoved(Object o) {
		isRemovedInput = o;
		return isRemovedOutput;
	}

	@Override
	public boolean isAttached(Object o) {
		return false;
	}

	@Override
	public boolean isSourceAttached(Object o) {
		throw new UnsupportedOperationException("isSourceAttached not supported by ModelServiceSpy");
	}

	@Override
	public void enableTransactions() {

	}

	@Override
	public void disableTransactions() {

	}

	@Override
	public void clearTransactionsSettings() {

	}

	@Override
	public <T> T getByExample(T t) {
		return null;
	}

	@Override
	public void lock(PK pk) {

	}

	@Override
	public void lock(Object o) {

	}

	@Override
	public <T> T getWithLock(Object o) {
		return null;
	}
}
