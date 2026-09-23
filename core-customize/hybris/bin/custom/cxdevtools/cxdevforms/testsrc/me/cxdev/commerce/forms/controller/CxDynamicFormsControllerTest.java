package me.cxdev.commerce.forms.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.webservicescommons.mapping.DataMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import me.cxdev.commerce.forms.data.DynamicFormData;
import me.cxdev.commerce.forms.dto.DynamicFormWsDTO;
import me.cxdev.commerce.forms.facade.DynamicFormFacade;

@UnitTest
@ExtendWith(MockitoExtension.class)
class CxDynamicFormsControllerTest {
	@Mock
	private DynamicFormFacade dynamicFormFacade;
	@Mock
	private DataMapper dataMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		final CxDynamicFormsController controller = new CxDynamicFormsController();
		ReflectionTestUtils.setField(controller, "dynamicFormFacade", dynamicFormFacade);
		ReflectionTestUtils.setField(controller, "dataMapper", dataMapper);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.build();
	}

	@Test
	void shouldReturnAllDynamicForms() throws Exception {
		when(dynamicFormFacade.getAllDynamicForms()).thenReturn(List.of(new DynamicFormData()));
		when(dataMapper.mapAsList(anyList(), eq(DynamicFormWsDTO.class), eq("DEFAULT"))).thenReturn(List.of(new DynamicFormWsDTO()));

		mockMvc.perform(get("/baseSiteId/forms"))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturnDynamicFormById() throws Exception {
		final DynamicFormData formData = new DynamicFormData();
		when(dynamicFormFacade.getDynamicFormForId("request-form")).thenReturn(formData);
		when(dataMapper.map(formData, DynamicFormWsDTO.class, "DEFAULT")).thenReturn(new DynamicFormWsDTO());

		mockMvc.perform(get("/baseSiteId/forms/request-form"))
				.andExpect(status().isOk());

		verify(dynamicFormFacade).getDynamicFormForId("request-form");
	}
}
