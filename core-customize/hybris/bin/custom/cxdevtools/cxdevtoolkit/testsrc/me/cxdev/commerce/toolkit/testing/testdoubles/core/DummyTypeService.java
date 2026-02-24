package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.enumeration.EnumerationMetaTypeModel;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.core.model.type.AtomicTypeModel;
import de.hybris.platform.core.model.type.AttributeDescriptorModel;
import de.hybris.platform.core.model.type.ComposedTypeModel;
import de.hybris.platform.core.model.type.TypeModel;
import de.hybris.platform.servicelayer.type.AttributeModifierCriteria;
import de.hybris.platform.servicelayer.type.TypeService;

public class DummyTypeService implements TypeService {
	public ComposedTypeModel type;

	@Override
	public ComposedTypeModel getComposedType(String paramString) {
		return null;
	}

	@Override
	public ComposedTypeModel getComposedTypeForCode(String paramString) {
		return null;
	}

	@Override
	public ComposedTypeModel getComposedType(Class paramClass) {
		return null;
	}

	@Override
	public ComposedTypeModel getComposedTypeForClass(Class paramClass) {
		return type;
	}

	@Override
	public AttributeDescriptorModel getAttributeDescriptor(ComposedTypeModel paramComposedTypeModel, String paramString) {
		return null;
	}

	@Override
	public AttributeDescriptorModel getAttributeDescriptor(String paramString1, String paramString2) {
		return null;
	}

	@Override
	public Set<AttributeDescriptorModel> getAttributeDescriptors(ComposedTypeModel paramComposedTypeModel) {
		return null;
	}

	@Override
	public Set<AttributeDescriptorModel> getAttributeDescriptorsForType(ComposedTypeModel paramComposedTypeModel) {
		return null;
	}

	@Override
	public Set<AttributeDescriptorModel> getAttributeDescriptors(String paramString) {
		return null;
	}

	@Override
	public Set<AttributeDescriptorModel> getInitialAttributeDescriptorsForType(ComposedTypeModel paramComposedTypeModel) {
		return null;
	}

	@Override
	public AtomicTypeModel getAtomicType(String paramString) {
		return null;
	}

	@Override
	public AtomicTypeModel getAtomicTypeForCode(String paramString) {
		return null;
	}

	@Override
	public AtomicTypeModel getAtomicTypeForJavaClass(Class paramClass) {
		return null;
	}

	@Override
	public TypeModel getType(String paramString) {
		return null;
	}

	@Override
	public TypeModel getTypeForCode(String paramString) {
		return null;
	}

	@Override
	public EnumerationMetaTypeModel getEnumerationType(String paramString) {
		return null;
	}

	@Override
	public EnumerationMetaTypeModel getEnumerationTypeForCode(String paramString) {
		return null;
	}

	@Override
	public EnumerationValueModel getEnumerationValue(String paramString1, String paramString2) {
		return null;
	}

	@Override
	public EnumerationValueModel getEnumerationValue(HybrisEnumValue paramHybrisEnumValue) {
		return null;
	}

	@Override
	public Set<String> getMandatoryAttributes(String paramString, boolean paramBoolean) {
		return null;
	}

	@Override
	public Set<String> getPartOfAttributes(final String s) {
		return null;
	}

	@Override
	public Set<String> getUniqueAttributes(String paramString) {
		return null;
	}

	@Override
	public String getUniqueModelRootType(String paramString) {
		return null;
	}

	@Override
	public Map<String, Object> getDefaultValues(String paramString) {
		return null;
	}

	@Override
	public Map<String, Object> getDefaultValues(String paramString, Collection<String> paramCollection) {
		return null;
	}

	@Override
	public boolean isAssignableFrom(TypeModel paramTypeModel1, TypeModel paramTypeModel2) {
		return false;
	}

	@Override
	public boolean isAssignableFrom(String paramString1, String paramString2) {
		return false;
	}

	@Override
	public boolean isInstance(TypeModel paramTypeModel, Object paramObject) {
		return false;
	}

	@Override
	public boolean hasAttribute(ComposedTypeModel paramComposedTypeModel, String paramString) {
		return false;
	}

	@Override
	public <T extends ItemModel> Class<T> getModelClass(ComposedTypeModel paramComposedTypeModel) {
		return null;
	}

	@Override
	public <T extends ItemModel> Class<T> getModelClass(String paramString) {
		return null;
	}

	@Override
	public Set<AttributeDescriptorModel> getAttributesForModifiers(String paramString,
			AttributeModifierCriteria paramAttributeModifierCriteria) {
		return null;
	}
}
