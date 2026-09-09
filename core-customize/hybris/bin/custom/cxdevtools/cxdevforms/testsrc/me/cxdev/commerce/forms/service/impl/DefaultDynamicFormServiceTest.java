package me.cxdev.commerce.forms.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;

import me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;
import me.cxdev.commerce.forms.model.DynamicFormModel;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultDynamicFormServiceTest {

	@Mock
	private GenericDao<DynamicFormModel> dynamicFormDao;

	private DefaultDynamicFormService systemUnderTest;

	@BeforeEach
	void setUp() {
		systemUnderTest = new DefaultDynamicFormService(dynamicFormDao);
	}

	@Test
	void shouldReturnImmutableCopyOfAllDynamicForms() {
		final DynamicFormModel form = InMemoryModelFactory.createTestableItemModel(DynamicFormModel.class);
		final List<DynamicFormModel> forms = new ArrayList<>(List.of(form));

		when(dynamicFormDao.find()).thenReturn(forms);

		final List<DynamicFormModel> result = systemUnderTest.getAllDynamicForms();

		assertEquals(List.of(form), result);
		assertThrows(UnsupportedOperationException.class, () -> result.add(form));
		verify(dynamicFormDao).find();
	}

	@Test
	void shouldReturnFirstDynamicFormForId() {
		final String formId = "technical-support";

		final DynamicFormModel first = InMemoryModelFactory.createTestableItemModel(DynamicFormModel.class);
		final DynamicFormModel second = InMemoryModelFactory.createTestableItemModel(DynamicFormModel.class);

		when(dynamicFormDao.find(Map.of(DynamicFormModel.ID, formId))).thenReturn(List.of(first, second));

		final Optional<DynamicFormModel> result = systemUnderTest.getDynamicFormForId(formId);

		assertTrue(result.isPresent());
		assertSame(first, result.orElseThrow());
	}

	@Test
	void shouldReturnEmptyOptionalWhenNoDynamicFormExistsForId() {
		when(dynamicFormDao.find(Map.of(DynamicFormModel.ID, "missing"))).thenReturn(null);

		assertTrue(systemUnderTest.getDynamicFormForId("missing").isEmpty());
	}
}
