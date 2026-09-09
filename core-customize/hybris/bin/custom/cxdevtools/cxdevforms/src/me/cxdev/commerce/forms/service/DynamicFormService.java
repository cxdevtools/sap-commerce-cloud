package me.cxdev.commerce.forms.service;

import java.util.List;
import java.util.Optional;

import me.cxdev.commerce.forms.model.DynamicFormModel;

/**
 * Service API for loading configured service request forms.
 */
public interface DynamicFormService {

	/**
	 * Returns all configured service request forms.
	 *
	 * @return all service request forms
	 */
	List<DynamicFormModel> getAllDynamicForms();

	/**
	 * Returns a service request form by its identifier.
	 *
	 * @param id the form identifier
	 * @return the matching form or {@code null} if no form exists
	 */
	Optional<DynamicFormModel> getDynamicFormForId(String id);

}
