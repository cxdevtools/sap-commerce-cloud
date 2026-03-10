package me.cxdev.commerce.toolkit.impex;

import java.util.Arrays;
import java.util.HashMap;

import de.hybris.platform.servicelayer.i18n.util.LanguageUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImpexLocalizationHelper {
	private static final Logger LOG = LoggerFactory.getLogger(ImpexLocalizationHelper.class);

	public static boolean languageSupported(final String isocode) {
		return LanguageUtils.isLanguagePresent(isocode);
	}

	public static boolean languageSupported(final String isocode, String languages) {
		if (StringUtils.isBlank(languages)) {
			return false;
		}

		boolean isContainedInList = Arrays.stream(StringUtils.split(languages, ","))
				.map(StringUtils::trimToEmpty)
				.anyMatch(isocode::equalsIgnoreCase);
		return isContainedInList && languageSupported(isocode);
	}

	public static void filterUnlocalizedLine(HashMap<Integer, String> valueLine, Integer column) {
		if (isLocalizedLine(valueLine, column)) {
			valueLine.clear();
		}
	}

	public static void filterUnlocalizedOrPrimaryLine(HashMap<Integer, String> valueLine, Integer column, String isocode) {
		if (isLocalizedLine(valueLine, column) && !hasCorrectLocale(valueLine, column, isocode)) {
			valueLine.clear();
		}
	}

	public static void filterLocalizedLine(HashMap<Integer, String> valueLine, Integer column, String isocode) {
		if (isUnlocalizedLine(valueLine, column) || !hasCorrectLocale(valueLine, column, isocode)) {
			valueLine.clear();
		}
	}

	public static boolean isLocalizedLine(HashMap<Integer, String> valueLine, Integer column) {
		return StringUtils.isNotBlank(valueLine.get(column));
	}

	public static boolean isUnlocalizedLine(HashMap<Integer, String> valueLine, Integer column) {
		return !isLocalizedLine(valueLine, column);
	}

	public static boolean hasCorrectLocale(HashMap<Integer, String> valueLine, Integer column, String isocode) {
		return (StringUtils.isBlank(isocode) && StringUtils.isBlank(valueLine.get(column)))
				|| StringUtils.equalsIgnoreCase(valueLine.get(column), isocode);
	}
}
