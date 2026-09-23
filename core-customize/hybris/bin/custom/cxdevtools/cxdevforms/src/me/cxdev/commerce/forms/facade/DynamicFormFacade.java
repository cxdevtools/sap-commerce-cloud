package me.cxdev.commerce.forms.facade;

import java.util.List;

import me.cxdev.commerce.forms.data.DynamicFormData;

/**
 * This facade summarizes functions around the service request forms.
 */
public interface DynamicFormFacade {
	/**
	 * Returns a list of all service request forms.
	 *
	 * @return list data of all service request forms
	 */
	List<DynamicFormData> getAllDynamicForms();

	/**
	 * Find service request form for given id.
	 *
	 * @param id id of the service request form
	 * @return service request form data
	 */
	DynamicFormData getDynamicFormForId(String id);

}
