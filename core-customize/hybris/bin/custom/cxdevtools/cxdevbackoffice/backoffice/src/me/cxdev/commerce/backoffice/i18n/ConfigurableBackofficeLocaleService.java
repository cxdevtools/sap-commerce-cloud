package me.cxdev.commerce.backoffice.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hybris.backoffice.i18n.BackofficeLocaleService;

import de.hybris.platform.servicelayer.i18n.I18NService;
import de.hybris.platform.servicelayer.user.UserService;

/**
 * Enhancement of the {@link BackofficeLocaleService} to enhance configuration possibilities!
 * <p>
 * This class introduces the configuration possibilities for activating a sort order based on the ISO code,
 * such that all data locales appear in the order of their ISO code within the backoffice cockpit. To activate
 * this feature, simply set the {@code orderLanguagesByIsoCode} property to {@code true}.
 * <p>
 * In addition, the UI locales can be limited by define a comma-separated list of ISO codes.
 */
public class ConfigurableBackofficeLocaleService extends BackofficeLocaleService {
	private UserService userService;
	private I18NService i18nService;
	private Comparator<Locale> localeComparator = Comparator.comparing(Locale::toString);

	private boolean sortDataLocalesByIsoCode = false;
	private List<String> localesForBackofficeUi = new LinkedList<>();
	private List<Locale> uiLocales;

	@Override
	public List<Locale> getAllLocales() {
		if (askForAllLocalesOnLoginScreen()) {
			return getAllUILocales();
		} else {
			return getAllSupportedLocales();
		}
	}

	private boolean askForAllLocalesOnLoginScreen() {
		return "anonymous".equals(userService.getCurrentUser().getUid());
	}

	@Override
	public List<Locale> getAllUILocales() {
		if (this.uiLocales == null) {
			this.cxInitializeUILocales();
		}
		return this.uiLocales;
	}

	private synchronized void cxInitializeUILocales() {
		if (this.uiLocales == null) {
			Map<String, Locale> allLocales = getAllSupportedLocales().stream()
					.collect(Collectors.toMap(Locale::toString, Function.identity()));
			List<Locale> tmpLocales = new ArrayList<>(localesForBackofficeUi.size());
			for (String configuredLocale : localesForBackofficeUi) {
				if (allLocales.containsKey(configuredLocale)) {
					tmpLocales.add(allLocales.get(configuredLocale));
				}
			}

			// With fallback to all supported locales, if no locale matches
			this.uiLocales = Collections.unmodifiableList(
					tmpLocales.isEmpty() ? getAllSupportedLocales() : tmpLocales);
		}
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Override
	public void setI18nService(I18NService i18nService) {
		super.setI18nService(i18nService);
		this.i18nService = i18nService;
	}

	public void setSortDataLocalesByIsoCode(boolean sortDataLocalesByIsoCode) {
		this.sortDataLocalesByIsoCode = sortDataLocalesByIsoCode;
	}

	public void setLocalesForBackofficeUi(String localesForBackofficeUi) {
		this.localesForBackofficeUi.clear();

		if (localesForBackofficeUi != null && !localesForBackofficeUi.isEmpty()) {
			String[] isoCodes = localesForBackofficeUi.split(",");
			for (String isoCode : isoCodes) {
				this.localesForBackofficeUi.add(isoCode.trim());
			}
		}
	}

	private List<Locale> getAllSupportedLocales() {
		ArrayList<Locale> supportedLocales = new ArrayList<>(this.i18nService.getSupportedLocales());
		if (sortDataLocalesByIsoCode) {
			supportedLocales.sort(localeComparator);
		}
		return supportedLocales;
	}
}
