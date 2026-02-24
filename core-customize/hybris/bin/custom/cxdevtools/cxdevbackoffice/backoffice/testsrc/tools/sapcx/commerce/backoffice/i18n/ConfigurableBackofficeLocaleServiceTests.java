package me.cxdev.commerce.backoffice.i18n;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hybris.bootstrap.annotations.UnitTest;

import org.junit.Before;
import org.junit.Test;

import me.cxdev.commerce.toolkit.testing.testdoubles.core.I18NServiceStubForLocales;
import me.cxdev.commerce.toolkit.testing.testdoubles.user.UserServiceFake;

@UnitTest
public class ConfigurableBackofficeLocaleServiceTests {
	private List<Locale> localesFromI18NService = new ArrayList<>();
	private ConfigurableBackofficeLocaleService service;
	private UserServiceFake userService;

	@Before
	public void setUp() throws Exception {
		service = new ConfigurableBackofficeLocaleService();
		userService = UserServiceFake.fake();
		service.setUserService(userService);
	}

	@Test
	public void withoutConfiguration_ReturnsUnmodifiedResultsFromI18NService() throws Exception {
		localesFromI18NService.addAll(List.of(Locale.ENGLISH, Locale.GERMAN, Locale.US, Locale.UK, Locale.GERMANY));

		service.setI18nService(new I18NServiceStubForLocales(localesFromI18NService));
		service.setSortDataLocalesByIsoCode(false);
		service.setLocalesForBackofficeUi("");

		assertThat(service.getAllUILocales())
				.describedAs("Expected all UI locales to be identical with list from i18n service!")
				.containsExactlyElementsOf(localesFromI18NService);
		assertThat(service.getAllLocales())
				.describedAs("Expected all locales to be identical with list from i18n service!")
				.containsExactlyElementsOf(localesFromI18NService);
	}

	@Test
	public void withConfiguration_ReturnsReducedSetOfUiLocalesInTheCorrectOrder() throws Exception {
		localesFromI18NService.addAll(List.of(Locale.ENGLISH, Locale.GERMAN, Locale.US, Locale.UK, Locale.GERMANY));

		service.setI18nService(new I18NServiceStubForLocales(localesFromI18NService));
		service.setSortDataLocalesByIsoCode(true);
		service.setLocalesForBackofficeUi("en_US,de_DE");

		assertThat(service.getAllUILocales())
				.describedAs("Expected all UI locales to be limited to en_US and de_DE!")
				.containsExactly(Locale.US, Locale.GERMANY);
		assertThat(service.getAllLocales())
				.describedAs("Expected all locales to be sorted by ISO code!")
				.containsExactly(Locale.GERMAN, Locale.GERMANY, Locale.ENGLISH, Locale.UK, Locale.US);
	}

	@Test
	public void withInvalidConfiguration_ReturnsUnmodifiedResultsFromSuperclass() throws Exception {
		localesFromI18NService.addAll(List.of(Locale.ENGLISH, Locale.GERMAN, Locale.US, Locale.UK, Locale.GERMANY));

		service.setI18nService(new I18NServiceStubForLocales(localesFromI18NService));
		service.setSortDataLocalesByIsoCode(true);
		service.setLocalesForBackofficeUi("unknown");

		assertThat(service.getAllUILocales())
				.describedAs("Expected all UI locales to be identical with list from i18n service!")
				.containsExactly(Locale.GERMAN, Locale.GERMANY, Locale.ENGLISH, Locale.UK, Locale.US);
		assertThat(service.getAllLocales())
				.describedAs("Expected all locales to be sorted by ISO code!")
				.containsExactly(Locale.GERMAN, Locale.GERMANY, Locale.ENGLISH, Locale.UK, Locale.US);
	}

	@Test
	public void forLoginPageAsAnonymousUser_returnsAllUILocalesForBoth() throws Exception {
		localesFromI18NService.addAll(List.of(Locale.ENGLISH, Locale.GERMAN, Locale.US, Locale.UK, Locale.GERMANY));

		service.setI18nService(new I18NServiceStubForLocales(localesFromI18NService));
		service.setSortDataLocalesByIsoCode(true);
		service.setLocalesForBackofficeUi("en_US,de_DE");

		userService.setCurrentUser(userService.getAnonymousUser());

		assertThat(service.getAllUILocales())
				.describedAs("Expected all UI locales to be limited to en_US and de_DE!")
				.containsExactly(Locale.US, Locale.GERMANY);
		assertThat(service.getAllLocales())
				.describedAs("Expected all locales to be limited to en_US and de_DE for anonymous users (on Login Page)!")
				.containsExactly(Locale.US, Locale.GERMANY);
	}
}
