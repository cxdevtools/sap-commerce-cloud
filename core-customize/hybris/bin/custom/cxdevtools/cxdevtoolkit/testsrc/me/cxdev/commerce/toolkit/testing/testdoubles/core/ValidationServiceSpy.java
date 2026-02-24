package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import java.util.Collection;
import java.util.Set;

import de.hybris.platform.validation.exceptions.HybrisConstraintViolation;
import de.hybris.platform.validation.model.constraints.AbstractConstraintModel;
import de.hybris.platform.validation.model.constraints.ConstraintGroupModel;
import de.hybris.platform.validation.services.ValidationService;

public class ValidationServiceSpy implements ValidationService {
	public int reloadValidationEngineCount;

	@Override
	public void reloadValidationEngine() {
		reloadValidationEngineCount++;
	}

	@Override
	public <T> Set<HybrisConstraintViolation> validate(final T t, final Collection<ConstraintGroupModel> collection) {
		return null;
	}

	@Override
	public <T> Set<HybrisConstraintViolation> validate(final T t, final Class<?>... classes) {
		return null;
	}

	@Override
	public <T> Set<HybrisConstraintViolation> validateProperty(final T t, final String s, final Collection<ConstraintGroupModel> collection) {
		return null;
	}

	@Override
	public <T> Set<HybrisConstraintViolation> validateProperty(final T t, final String s, final Class<?>... classes) {
		return null;
	}

	@Override
	public <T> Set<HybrisConstraintViolation> validateValue(final Class<T> aClass, final String s, final Object o,
			final Collection<ConstraintGroupModel> collection) {
		return null;
	}

	@Override
	public <T> Set<HybrisConstraintViolation> validateValue(final Class<T> aClass, final String s, final Object o, final Class<?>... classes) {
		return null;
	}

	@Override
	public void setActiveConstraintGroups(final Collection<ConstraintGroupModel> collection) {

	}

	@Override
	public Collection<ConstraintGroupModel> getActiveConstraintGroups() {
		return null;
	}

	@Override
	public void unloadValidationEngine() {

	}

	@Override
	public ConstraintGroupModel getDefaultConstraintGroup() {
		return null;
	}

	@Override
	public boolean needReloadOfValidationEngine(final AbstractConstraintModel abstractConstraintModel) {
		return false;
	}
}
