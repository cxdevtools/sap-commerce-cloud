package tools.sapcx.commerce.search.provider;

import java.util.Locale;

import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.solrfacetsearch.config.IndexedProperty;
import de.hybris.platform.solrfacetsearch.provider.FacetDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.provider.FacetValueDisplayNameProvider;
import de.hybris.platform.solrfacetsearch.search.SearchQuery;

public class CxHybrisEnumFacetDisplayNameProvider implements FacetDisplayNameProvider, FacetValueDisplayNameProvider {
	private String enumType;
	private TypeService typeService;
	private CommonI18NService commonI18NService;

	public CxHybrisEnumFacetDisplayNameProvider(String enumType, TypeService typeService, CommonI18NService commonI18NService) {
		this.enumType = enumType;
		this.typeService = typeService;
		this.commonI18NService = commonI18NService;
	}

	@Override
	public String getDisplayName(SearchQuery searchQuery, String value) {
		try {
			EnumerationValueModel enumValue = typeService.getEnumerationValue(enumType, value);
			LanguageModel language = commonI18NService.getLanguage(searchQuery.getLanguage());
			Locale locale = commonI18NService.getLocaleForLanguage(language);
			return enumValue.getName(locale);
		} catch (RuntimeException e) {
			return value;
		}
	}

	@Override
	public String getDisplayName(SearchQuery searchQuery, IndexedProperty indexedProperty, String value) {
		return getDisplayName(searchQuery, value);
	}
}
