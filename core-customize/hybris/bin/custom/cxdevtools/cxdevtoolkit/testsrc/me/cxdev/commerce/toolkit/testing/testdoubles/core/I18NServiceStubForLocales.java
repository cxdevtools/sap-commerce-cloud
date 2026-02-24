package me.cxdev.commerce.toolkit.testing.testdoubles.core;

import java.util.ArrayList;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;

import de.hybris.platform.core.HybrisEnumValue;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.servicelayer.i18n.I18NService;

public class I18NServiceStubForLocales implements I18NService {
	private Locale currentLocale;
	private List<Locale> allLocales;

	public I18NServiceStubForLocales(List<Locale> locales) {
		this.allLocales = new ArrayList<>(locales);
		this.currentLocale = locales.isEmpty() ? null : locales.get(0);
	}

	@Override
	public Locale getCurrentLocale() {
		return currentLocale;
	}

	@Override
	public void setCurrentLocale(Locale locale) {
		if (!allLocales.contains(locale)) {
			allLocales.add(locale);
		}
		currentLocale = locale;
	}

	@Override
	public Set<Locale> getSupportedLocales() {
		return new LinkedHashSet<>(allLocales);
	}

	@Override
	public Set<Currency> getSupportedJavaCurrencies() {
		return null;
	}

	@Override
	public TimeZone getCurrentTimeZone() {
		return null;
	}

	@Override
	public void setCurrentTimeZone(TimeZone timeZone) {

	}

	@Override
	public Currency getCurrentJavaCurrency() {
		return null;
	}

	@Override
	public void setCurrentJavaCurrency(Currency currency) {

	}

	@Override
	public boolean isLocalizationFallbackEnabled() {
		return false;
	}

	@Override
	public void setLocalizationFallbackEnabled(boolean b) {

	}

	@Override
	public Currency getBestMatchingJavaCurrency(String s) {
		return null;
	}

	@Override
	public Locale getBestMatchingLocale(Locale locale) {
		return null;
	}

	@Override
	public Locale[] getAllLocales(Locale locale) {
		return allLocales.toArray(new Locale[] {});
	}

	@Override
	public Locale[] getFallbackLocales(Locale locale) {
		return new Locale[] { currentLocale };
	}

	@Override
	public CurrencyModel getCurrentCurrency() {
		return null;
	}

	@Override
	public void setCurrentCurrency(CurrencyModel currencyModel) {

	}

	@Override
	public Set<Locale> getSupportedDataLocales() {
		return new LinkedHashSet<>(allLocales);
	}

	@Override
	public ResourceBundle getBundle(String s) {
		return null;
	}

	@Override
	public ResourceBundle getBundle(String s, Locale[] locales) {
		return null;
	}

	@Override
	public ResourceBundle getBundle(String s, Locale[] locales, ClassLoader classLoader) {
		return null;
	}

	@Override
	public LanguageModel getLanguage(String s) {
		return null;
	}

	@Override
	public Set<LanguageModel> getAllLanguages() {
		return null;
	}

	@Override
	public Set<LanguageModel> getAllActiveLanguages() {
		return null;
	}

	@Override
	public CountryModel getCountry(String s) {
		return null;
	}

	@Override
	public Set<CountryModel> getAllCountries() {
		return null;
	}

	@Override
	public CurrencyModel getCurrency(String s) {
		return null;
	}

	@Override
	public Set<CurrencyModel> getAllCurrencies() {
		return null;
	}

	@Override
	public CurrencyModel getBaseCurrency() {
		return null;
	}

	@Override
	public String getEnumLocalizedName(HybrisEnumValue hybrisEnumValue) {
		return null;
	}

	@Override
	public void setEnumLocalizedName(HybrisEnumValue hybrisEnumValue, String s) {

	}

	@Override
	public PK getLangPKFromLocale(Locale locale) {
		return null;
	}
}
