package me.cxdev.commerce.forms.controller;

import java.util.List;

import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiFieldsParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import jakarta.ws.rs.core.MediaType;
import me.cxdev.commerce.forms.data.DynamicFormData;
import me.cxdev.commerce.forms.dto.DynamicFormWsDTO;
import me.cxdev.commerce.forms.facade.DynamicFormFacade;

@RestController
@RequestMapping(value = "/{baseSiteId}/forms")
@Tag(name = "Dynamic Forms")
@ApiVersion("v2")
public class CxDynamicFormsController {
	protected static final String DEFAULT_FIELD_SET = "DEFAULT";

	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	@Resource(name = "cxDynamicFormFacade")
	private DynamicFormFacade dynamicFormFacade;

	@GetMapping()
	@Operation(operationId = "getAllDynamicForms", summary = "Get all dynamic forms", description = "Returns all dynamic forms with detailed information")
	public List<DynamicFormWsDTO> getAllDynamicForms(
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
		final List<DynamicFormData> requestForms = dynamicFormFacade.getAllDynamicForms();
		return dataMapper.mapAsList(requestForms, DynamicFormWsDTO.class, fields);
	}

	@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON)
	@Operation(operationId = "getDynamicFormForId", summary = "Returns dynamic form of given id.", description = "Returns dynamic form of given id.")
	public DynamicFormWsDTO getDynamicFormForId(@PathVariable("id") final String id,
			@ApiFieldsParam @RequestParam(defaultValue = DEFAULT_FIELD_SET) final String fields) {
		final DynamicFormData requestForm = dynamicFormFacade.getDynamicFormForId(id);
		return dataMapper.map(requestForm, DynamicFormWsDTO.class, fields);
	}
}
