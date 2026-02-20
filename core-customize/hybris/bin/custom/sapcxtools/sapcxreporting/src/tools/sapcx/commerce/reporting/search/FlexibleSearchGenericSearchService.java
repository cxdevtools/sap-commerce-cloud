package tools.sapcx.commerce.reporting.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jdbcwrapper.HybrisDataSource;
import de.hybris.platform.persistence.property.JDBCValueMappings;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.TranslationResult;
import de.hybris.platform.servicelayer.search.exceptions.FlexibleSearchException;
import de.hybris.platform.servicelayer.search.impl.DefaultFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionExecutionBody;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the {@link GenericSearchService} interface performing searches based on the {@link DefaultFlexibleSearchService}. It supports
 * flexible search queries as well as raw SQL search queries. When using raw SQL statements, be aware that the statements must match the
 * capabilities of the underlying {@link javax.sql.DataSource} and heavily depends upon its functionalities and features.
 */
public class FlexibleSearchGenericSearchService implements GenericSearchService {
	private static final Logger LOG = LoggerFactory.getLogger(FlexibleSearchGenericSearchService.class);
	private static final String LOG_MSG_TRANSLATION_INPUT = "FlexibleSearchQuery object for translation: [query: %s], [query parameters: %s], [user - %s], [count - -1], [locale - %s], [ctgVer - %s]";

	private final UserService userService;
	private final SessionService sessionService;
	private final CatalogVersionService catalogVersionService;
	private final DefaultFlexibleSearchService flexibleSearchService;

	public FlexibleSearchGenericSearchService(UserService userService, SessionService sessionService, CatalogVersionService catalogVersionService,
			DefaultFlexibleSearchService flexibleSearchService) {
		this.userService = userService;
		this.sessionService = sessionService;
		this.catalogVersionService = catalogVersionService;
		this.flexibleSearchService = flexibleSearchService;
	}

	@Override
	public GenericSearchResult search(String query, Map<String, Object> parameters, Map<String, Object> configuration) {
		if (StringUtils.isBlank(query)) {
			return error("Query statements must not be blank!");
		}

		try (Connection connection = getDatasource().getConnection()) {
			connection.setAutoCommit(false);
			TranslationResult translationResult = translateQuery(query, flexibleSearchService.toPersistenceLayer(parameters), userService.getCurrentUser(), Locale.ENGLISH);
			try (PreparedStatement preparedStatement = getPreparedStatement(connection, translationResult.getSQLQuery(), translationResult.getSQLQueryParameters())) {
				ResultSet resultSet = preparedStatement.executeQuery();
				ResultSetMetaData metaData = resultSet.getMetaData();
				List<GenericSearchResultHeader> headers = getHeaders(metaData);
				List<Map<GenericSearchResultHeader, String>> values = getValues(resultSet, headers);
				return new GenericSearchResult(headers, values);
			} finally {
				connection.rollback();
			}
		} catch (FlexibleSearchException | SQLException e) {
			LOG.error(String.format("Error during execution of query '%s' with parameters: '{%s}'", query, parameters), e);
			return error(e.getMessage());
		}
	}

	@VisibleForTesting
	HybrisDataSource getDatasource() {
		return Registry.getCurrentTenant().getDataSource();
	}

	/**
	 * Translates a flexible search query into an SQL query by making use of a {@link Execution} object.
	 * <p>
	 * Note: The logic for this part has its origin in {@code de.hybris.platform.hac.facade.impl.DefaultFlexibleSearchFacade}.
	 */
	private TranslationResult translateQuery(final String query, Map<String, Object> parameters, final UserModel user, final Locale locale) {
		Collection<CatalogVersionModel> allReadableCatalogVersions = catalogVersionService.getAllReadableCatalogVersions(user);
		Collection<CatalogVersionModel> catalogVersions = allReadableCatalogVersions.isEmpty() ? catalogVersionService.getAllCatalogVersions() : allReadableCatalogVersions;
		return sessionService.executeInLocalView(new Execution(query, parameters, user, locale, catalogVersions));
	}

	/**
	 * Creates a prepared statement and fills parameters.
	 * <p>
	 * Note: The logic for this part has its origin in {@code de.hybris.platform.hac.facade.impl.DefaultFlexibleSearchFacade}.
	 */
	private PreparedStatement getPreparedStatement(Connection connection, String sqlQuery, List<Object> queryParams) throws SQLException {
		int resultSetType = isHanaUsed() ? ResultSet.TYPE_FORWARD_ONLY : ResultSet.TYPE_SCROLL_INSENSITIVE;
		PreparedStatement statement = connection.prepareStatement(sqlQuery, resultSetType, ResultSet.CONCUR_READ_ONLY);
		if (CollectionUtils.isNotEmpty(queryParams)) {
			fillStatement(statement, queryParams);
		}
		return statement;
	}

	protected boolean isHanaUsed() {
		return Config.DatabaseNames.HANA.equals(getDatasource().getDatabaseName());
	}

	/**
	 * Fills prepared statement with the provided parameters.
	 * <p>
	 * Note: The logic for this part has its origin in {@code de.hybris.platform.hac.facade.impl.DefaultFlexibleSearchFacade}.
	 */
	protected void fillStatement(PreparedStatement statement, List<Object> values) throws IllegalArgumentException, SQLException {
		JDBCValueMappings.getInstance().fillStatement(statement, values);
	}

	private GenericSearchResult error(String message) {
		return new GenericSearchResult(message);
	}

	private List<GenericSearchResultHeader> getHeaders(ResultSetMetaData metaData) throws SQLException {
		List<GenericSearchResultHeader> headers = new ArrayList<>(metaData.getColumnCount());
		// Note: headers indexes from the database are one-based!
		for (int columnIndex = 1; columnIndex <= metaData.getColumnCount(); columnIndex++) {
			headers.add(getHeader(metaData, columnIndex));
		}
		return headers;
	}

	private GenericSearchResultHeader getHeader(ResultSetMetaData metaData, int columnIndex) {
		String columnName = getColumnName(metaData, columnIndex);
		String columnLabel = getColumnLabel(metaData, columnIndex);
		return new GenericSearchResultHeader(columnIndex, columnName, columnLabel);
	}

	private String getColumnName(ResultSetMetaData metaData, int columnIndex) {
		try {
			return metaData.getColumnName(columnIndex);
		} catch (SQLException e) {
			LOG.warn("Could not get column name", e);
			return "";
		}
	}

	private String getColumnLabel(ResultSetMetaData metaData, int columnIndex) {
		try {
			return metaData.getColumnLabel(columnIndex);
		} catch (SQLException e) {
			LOG.warn("Could not get column label", e);
			return "";
		}
	}

	private List<Map<GenericSearchResultHeader, String>> getValues(ResultSet resultSet, List<GenericSearchResultHeader> headers) throws SQLException {
		List<Map<GenericSearchResultHeader, String>> valueLines = new ArrayList<>();
		while (resultSet.next()) {
			Map<GenericSearchResultHeader, String> valueLine = new LinkedHashMap<>();
			for (GenericSearchResultHeader header : headers) {
				valueLine.put(header, getValue(resultSet, header));
			}
			valueLines.add(valueLine);
		}
		return valueLines;
	}

	private String getValue(ResultSet resultSet, GenericSearchResultHeader columnHeader) {
		Exception suppressed;

		try {
			return resultSet.getString(columnHeader.getColumnLabel());
		} catch (SQLException e) {
			LOG.debug("Could not get value by label, continue by trying the name...");
			suppressed = e;
		}

		try {
			return resultSet.getString(columnHeader.getColumnName());
		} catch (SQLException e) {
			LOG.debug("Could not get value by column name, continue by trying the index!");
			e.addSuppressed(suppressed);
			suppressed = e;
		}

		try {
			return resultSet.getString(columnHeader.getColumnIndex());
		} catch (SQLException e) {
			LOG.debug("Could not get value by index, giving up...");
			e.addSuppressed(suppressed);
			LOG.warn(String.format("Could not obtain value for columnHeader '%s' in result set! Result may not be complete.", columnHeader), e);
		}

		return null;
	}

	private class Execution extends SessionExecutionBody {
		private String query;
		private Map<String, Object> parameters;
		private UserModel user;
		private Locale locale;
		private Collection<CatalogVersionModel> catalogVersions;

		public Execution(String query, Map<String, Object> parameters, UserModel user, Locale locale, Collection<CatalogVersionModel> catalogVersions) {
			this.query = query;
			this.parameters = parameters;
			this.user = user;
			this.locale = locale;
			this.catalogVersions = catalogVersions;
		}

		@Override
		public TranslationResult execute() {
			if (LOG.isDebugEnabled()) {
				LOG.debug(String.format(LOG_MSG_TRANSLATION_INPUT, query, parameters, user, locale, catalogVersions));
			}

			FlexibleSearchQuery fQuery = new FlexibleSearchQuery(query);
			fQuery.addQueryParameters(parameters);
			fQuery.setUser(user);
			fQuery.setLocale(locale);
			fQuery.setCount(-1);
			fQuery.setCatalogVersions(catalogVersions);
			return flexibleSearchService.translate(fQuery);
		}
	}
}
