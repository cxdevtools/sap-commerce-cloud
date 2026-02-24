package me.cxdev.commerce.toolkit.testing.testdoubles.dao;

import static me.cxdev.commerce.toolkit.testing.utils.ReflectionUtils.throwException;

import java.util.List;
import java.util.Map;

import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.internal.dao.SortParameters;

class GenericDaoFakeThrowingException<T extends RuntimeException> implements GenericDao {
	private Class<T> exceptionToThrow;

	public GenericDaoFakeThrowingException(Class<T> exceptionToThrow) {
		this.exceptionToThrow = exceptionToThrow;
	}

	@Override
	public List find() {
		throwException(exceptionToThrow);
		return null;
	}

	@Override
	public List find(SortParameters sortParameters) {
		throwException(exceptionToThrow);
		return null;
	}

	@Override
	public List find(Map map, SortParameters sortParameters, int i) {
		throwException(exceptionToThrow);
		return null;
	}

	@Override
	public List find(Map map, SortParameters sortParameters) {
		throwException(exceptionToThrow);
		return null;
	}

	@Override
	public List find(Map map) {
		throwException(exceptionToThrow);
		return null;
	}
}
