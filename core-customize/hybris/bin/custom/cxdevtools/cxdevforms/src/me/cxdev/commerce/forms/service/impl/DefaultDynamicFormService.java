package me.cxdev.commerce.forms.service.impl;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.hybris.platform.servicelayer.internal.dao.GenericDao;

import me.cxdev.commerce.forms.service.DynamicFormService;
import me.cxdev.commerce.forms.model.DynamicFormModel;

/**
 * Default implementation of {@link DynamicFormService}. Uses a {@link GenericDao} to retrieve form
 * configurations from the database.
 */
public class DefaultDynamicFormService implements DynamicFormService {
	private final GenericDao<DynamicFormModel> dynamicFormDao;

	public DefaultDynamicFormService(final GenericDao<DynamicFormModel> dynamicFormDao) {
		this.dynamicFormDao = dynamicFormDao;
	}

	/**
	 * Returns all configured dynamic forms.
	 *
	 * @return all forms
	 */
	@Override
	public List<DynamicFormModel> getAllDynamicForms() {
		return List.copyOf(dynamicFormDao.find());
	}

	/**
	 * Returns a dynamic form for a given id.
	 *
	 * @param id the form id
	 * @return the matching form or {@code null}
	 */
	@Override
	public Optional<DynamicFormModel> getDynamicFormForId(final String id) {
		return emptyIfNull(dynamicFormDao.find(
				Map.of(DynamicFormModel.ID, id)))
				.stream()
				.findFirst();
	}
}
