package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.PersistenceOperation;
import de.hybris.platform.servicelayer.model.ModelService;

public class InterceptorContextStub implements InterceptorContext {
	public static Builder interceptorContext() {
		return new Builder();
	}

	public static class Builder {
		InterceptorContextStub stub = new InterceptorContextStub();

		public Builder withNew(boolean asNew) {
			stub.isNew = asNew;
			return this;
		}

		public Builder withExists(boolean asExists) {
			stub.exists = asExists;
			return this;
		}

		public Builder withModified(boolean asModified) {
			stub.isModified = asModified;
			return this;
		}

		public Builder withModifiedAttribute(String attributeName) {
			stub.isModified = true;
			stub.modifiedAttributeNames.add(attributeName);
			return this;
		}

		public Builder withRemoved(boolean asRemoved) {
			stub.isRemoved = asRemoved;
			return this;
		}

		public InterceptorContextStub stub() {
			return stub;
		}
	}

	private boolean isNew;
	private boolean exists;
	private boolean isModified;
	private List<String> modifiedAttributeNames = new ArrayList<>();
	private boolean isRemoved;

	private InterceptorContextStub() {
	}

	@Override
	public Object getSource(Object o) {
		throw new UnsupportedOperationException("method is deprecated!");
	}

	@Override
	public Set<Object> getAllRegisteredElements() {
		throw new UnsupportedOperationException("method is deprecated!");
	}

	@Override
	public Set<Object> getElementsRegisteredFor(PersistenceOperation persistenceOperation) {
		throw new UnsupportedOperationException("method is deprecated!");
	}

	@Override
	public void registerElement(Object o, Object o1) {
		throw new UnsupportedOperationException("method is deprecated!");
	}

	@Override
	public void registerElement(Object o) {
		throw new UnsupportedOperationException("method is deprecated!");
	}

	@Override
	public void registerElementFor(Object o, PersistenceOperation persistenceOperation) {
		throw new UnsupportedOperationException("method is deprecated!");
	}

	@Override
	public boolean contains(Object o) {
		return true;
	}

	@Override
	public boolean contains(Object o, PersistenceOperation persistenceOperation) {
		return true;
	}

	@Override
	public boolean isNew(Object o) {
		return isNew;
	}

	@Override
	public boolean exists(Object o) {
		return exists;
	}

	@Override
	public boolean isModified(Object o) {
		return isModified;
	}

	@Override
	public boolean isModified(Object o, String attributeName) {
		return isModified && modifiedAttributeNames.contains(attributeName);
	}

	@Override
	public boolean isRemoved(Object o) {
		return isRemoved;
	}

	@Override
	public ModelService getModelService() {
		return null;
	}

	@Override
	public Object getAttribute(String s) {
		return null;
	}

	@Override
	public void setAttribute(String s, Object o) {
	}

	@Override
	public Map<String, Set<Locale>> getDirtyAttributes(Object o) {
		return null;
	}

	@Override
	public TransientStorage getTransientStorage() {
		return null;
	}
}
