package tools.sapcx.commerce.reporting.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.EmployeeModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jdbcwrapper.HybrisDataSource;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.TranslationResult;
import de.hybris.platform.servicelayer.search.exceptions.FlexibleSearchException;
import de.hybris.platform.servicelayer.search.impl.DefaultFlexibleSearchService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import tools.sapcx.commerce.toolkit.testing.itemmodel.InMemoryModelFactory;
import tools.sapcx.commerce.toolkit.testing.testdoubles.catalog.CatalogVersionServiceFake;
import tools.sapcx.commerce.toolkit.testing.testdoubles.user.SessionServiceFake;
import tools.sapcx.commerce.toolkit.testing.testdoubles.user.UserServiceFake;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultGenericFlexibleSearchTests {
	private static final String FLEXIBLESEARCH_ERROR = "query error";
	private static final String FLEXIBLE_SEARCH_QUERY = "SELECT {pk} FROM {Product} WHERE {catalogVersion} IN (?catalogVersion)";
	private static final ImmutableMap<String, Object> QUERY_PARAMS = ImmutableMap.of("catalogVersion", InMemoryModelFactory.createTestableItemModel(CatalogVersionModel.class));

	private Tenant tenant = mock(Tenant.class);
	private Connection connection = mock(Connection.class);
	private HybrisDataSource datasource = mock(HybrisDataSource.class);
	private DefaultFlexibleSearchService flexibleSearchService = mock(DefaultFlexibleSearchService.class);
	private SessionService sessionService = new SessionServiceFake();
	private CatalogVersionService catalogVersionService = new CatalogVersionServiceFake();
	private UserService userService = new UserServiceFake(getUser(EmployeeModel.class), getUser(CustomerModel.class), getUser(UserModel.class));

	private List<GenericSearchResultHeader> headers = new LinkedList<>();
	private List<Map<GenericSearchResultHeader, Object>> values = new ArrayList<>();
	private GenericSearchResultHeader header1;
	private GenericSearchResultHeader header2;
	private ResultSetMock resultSetFake;

	private FlexibleSearchGenericSearchService genericFlexibleSearch;

	@Before
	public void setup() throws SQLException {
		when(tenant.getDataSource()).thenReturn(datasource);
		when(datasource.getConnection()).thenReturn(connection);

		PreparedStatement statement = mock(PreparedStatement.class);
		resultSetFake = new ResultSetMock(headers, values);
		when(statement.executeQuery()).thenReturn(resultSetFake);

		when(connection.prepareStatement(any(), anyInt(), anyInt())).thenReturn(statement);
		when(flexibleSearchService.toPersistenceLayer(any())).thenReturn(Collections.emptyMap());
		when(flexibleSearchService.translate(any())).thenReturn(mock(TranslationResult.class));

		((CatalogVersionServiceFake) catalogVersionService)
				.setReadableCatalogVersions(Collections.singletonList(InMemoryModelFactory.createTestableItemModel(CatalogVersionModel.class)));

		genericFlexibleSearch = new FlexibleSearchGenericSearchService(userService, sessionService, catalogVersionService, flexibleSearchService) {
			@Override
			protected boolean isHanaUsed() {
				return false;
			}

			@Override
			protected void fillStatement(PreparedStatement statement, List<Object> values) throws IllegalArgumentException, SQLException {
				return;
			}

			@Override
			HybrisDataSource getDatasource() {
				return tenant.getDataSource();
			}
		};

		header1 = new GenericSearchResultHeader(1, "col1", "Column 1");
		header2 = new GenericSearchResultHeader(2, "col2", "Column 2");
		headers.add(header1);
		headers.add(header2);
	}

	@Test
	public void withEmptyQuery_returnsError() {
		GenericSearchResult result = genericFlexibleSearch.search("", Map.of());

		verify(flexibleSearchService, never()).toPersistenceLayer(any());
		verify(flexibleSearchService, never()).translate(any(FlexibleSearchQuery.class));

		assertThat(result).isNotNull();
		assertThat(result.hasError()).isTrue();
	}

	@Test
	public void searchResultWithColumnIndex() {
		values.add(ImmutableMap.of(header1, "First Row First Value", header2, "First Row Second Value"));
		values.add(ImmutableMap.of(header1, "Second Row First Value", header2, "Second Row Second Value"));

		GenericSearchResult result = genericFlexibleSearch.search(FLEXIBLE_SEARCH_QUERY, QUERY_PARAMS);

		verify(flexibleSearchService, times(1)).toPersistenceLayer(QUERY_PARAMS);
		verify(flexibleSearchService, times(1)).translate(any(FlexibleSearchQuery.class));

		assertThat(result).isNotNull();
		assertThat(result.hasError()).isFalse();
		assertThat(result.getHeaderNames()).hasSize(2);
		assertThat(result.getValues()).hasSize(2);
		assertThat(result.getValues().get(0)).containsExactly(
				entry(header1, "First Row First Value"),
				entry(header2, "First Row Second Value"));
		assertThat(result.getValues().get(1)).containsExactly(
				entry(header1, "Second Row First Value"),
				entry(header2, "Second Row Second Value"));
	}

	@Test
	public void searchResultWithColumnLabel() {
		values.add(ImmutableMap.of(header1, "First Row First Value", header2, "First Row Second Value"));
		values.add(ImmutableMap.of(header1, "Second Row First Value", header2, "Second Row Second Value"));

		resultSetFake.withMatchingHeaderLabel();

		GenericSearchResult result = genericFlexibleSearch.search(FLEXIBLE_SEARCH_QUERY, QUERY_PARAMS);

		verify(flexibleSearchService, times(1)).toPersistenceLayer(QUERY_PARAMS);
		verify(flexibleSearchService, times(1)).translate(any(FlexibleSearchQuery.class));

		assertThat(result).isNotNull();
		assertThat(result.hasError()).isFalse();
		assertThat(result.getHeaderNames()).hasSize(2);
		assertThat(result.getValues()).hasSize(2);
		assertThat(result.getValues().get(0)).containsExactly(
				entry(header1, "First Row First Value"),
				entry(header2, "First Row Second Value"));
		assertThat(result.getValues().get(1)).containsExactly(
				entry(header1, "Second Row First Value"),
				entry(header2, "Second Row Second Value"));
	}

	@Test
	public void searchResultWithColumnName() {
		values.add(ImmutableMap.of(header1, "First Row First Value", header2, "First Row Second Value"));
		values.add(ImmutableMap.of(header1, "Second Row First Value", header2, "Second Row Second Value"));

		resultSetFake.withMatchingHeaderName();

		GenericSearchResult result = genericFlexibleSearch.search(FLEXIBLE_SEARCH_QUERY, QUERY_PARAMS);

		verify(flexibleSearchService, times(1)).toPersistenceLayer(QUERY_PARAMS);
		verify(flexibleSearchService, times(1)).translate(any(FlexibleSearchQuery.class));

		assertThat(result).isNotNull();
		assertThat(result.hasError()).isFalse();
		assertThat(result.getHeaderNames()).hasSize(2);
		assertThat(result.getValues()).hasSize(2);
		assertThat(result.getValues().get(0)).containsExactly(
				entry(header1, "First Row First Value"),
				entry(header2, "First Row Second Value"));
		assertThat(result.getValues().get(1)).containsExactly(
				entry(header1, "Second Row First Value"),
				entry(header2, "Second Row Second Value"));
	}

	@Test
	public void search_error() {
		when(flexibleSearchService.translate(any())).thenThrow(new FlexibleSearchException(FLEXIBLESEARCH_ERROR));
		GenericSearchResult result = genericFlexibleSearch.search(FLEXIBLE_SEARCH_QUERY, QUERY_PARAMS);

		verify(flexibleSearchService, times(1)).toPersistenceLayer(QUERY_PARAMS);
		verify(flexibleSearchService, times(1)).translate(any(FlexibleSearchQuery.class));

		assertThat(result).isNotNull();
		assertThat(result.hasError()).isTrue();
		assertThat(result.getError()).isEqualTo(FLEXIBLESEARCH_ERROR);
		assertThat(result.getValues()).isEmpty();
		assertThat(result.getHeaderNames()).isEmpty();
	}

	private <T extends PrincipalModel> T getUser(Class<T> clazz) {
		return InMemoryModelFactory.createTestableItemModel(clazz);
	}
}
