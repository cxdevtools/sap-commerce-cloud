package tools.sapcx.commerce.search.provider;

import java.util.Comparator;

import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.FacetSortProvider;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.search.FacetValue;

public class CxHybrisEnumFacetSortProvider implements FacetSortProvider {
	private String enumType;
	private TypeService typeService;
	private Comparator<FacetValue> comparator;

	public CxHybrisEnumFacetSortProvider(String enumType, TypeService typeService) {
		this.enumType = enumType;
		this.typeService = typeService;
		this.comparator = (o1, o2) -> {
			EnumerationValueModel enum1 = this.typeService.getEnumerationValue(this.enumType, o1.getName());
			EnumerationValueModel enum2 = this.typeService.getEnumerationValue(this.enumType, o2.getName());
			return enum1.getSequenceNumber().compareTo(enum2.getSequenceNumber());
		};
	}

	@Override
	public Comparator<FacetValue> getComparatorForTypeAndProperty(IndexedType indexedType, IndexedProperty indexedProperty) {
		return comparator;
	}
}
