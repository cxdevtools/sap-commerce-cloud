package tools.sapcx.commerce.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfig;
import de.hybris.platform.solrfacetsearch.config.FacetSearchConfigService;
import de.hybris.platform.solrfacetsearch.config.IndexConfig;
import de.hybris.platform.solrfacetsearch.config.IndexOperation;
import de.hybris.platform.solrfacetsearch.config.IndexedType;
import de.hybris.platform.solrfacetsearch.indexer.IndexerService;
import de.hybris.platform.solrfacetsearch.indexer.exceptions.IndexerException;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategy;
import de.hybris.platform.solrfacetsearch.indexer.strategies.IndexerStrategyFactory;

import org.junit.Before;
import org.junit.Test;

import tools.sapcx.commerce.toolkit.testing.testdoubles.core.InMemoryModelService;

@UnitTest
public class DefaultCxIndexerServiceTests {
	private DefaultCxIndexerService defaultCxIndexerService;
	private IndexerStrategyFactory indexerStrategyFactoryMock;
	private IndexerStrategy indexerStrategyMock;
	private FacetSearchConfigService facetSearchConfigServiceMock;
	private IndexerService indexerServiceMock;
	private FacetSearchConfig facetSearchConfig;
	private InMemoryModelService modelService;
	private IndexConfig indexConfig;

	@Before
	public void setUp() throws Exception {
		indexerStrategyFactoryMock = mock(IndexerStrategyFactory.class);
		indexerStrategyMock = spy(IndexerStrategy.class);
		facetSearchConfigServiceMock = spy(FacetSearchConfigService.class);
		indexerServiceMock = spy(IndexerService.class);
		modelService = new InMemoryModelService();

		defaultCxIndexerService = new DefaultCxIndexerService(indexerStrategyFactoryMock, facetSearchConfigServiceMock, indexerServiceMock);

		IndexedType indexedTypeA = new IndexedType();
		indexedTypeA.setIdentifier("type-a");

		IndexedType indexedTypeB = new IndexedType();
		indexedTypeB.setIdentifier("type-b");

		IndexedType indexedTypeC = new IndexedType();
		indexedTypeC.setIdentifier("type-c");

		indexConfig = new IndexConfig();
		indexConfig.setIndexedTypes(Map.of("type-a", indexedTypeA, "type-b", indexedTypeB, "type-c", indexedTypeC));

		facetSearchConfig = new FacetSearchConfig();
		facetSearchConfig.setName("searchConfig");
		facetSearchConfig.setIndexConfig(indexConfig);

		when(facetSearchConfigServiceMock.getConfiguration("searchConfig")).thenReturn(facetSearchConfig);
		when(facetSearchConfigServiceMock.resolveIndexedType(facetSearchConfig, "type-a")).thenReturn(indexedTypeA);
		when(facetSearchConfigServiceMock.resolveIndexedType(facetSearchConfig, "type-b")).thenReturn(indexedTypeB);
		when(facetSearchConfigServiceMock.resolveIndexedType(facetSearchConfig, "type-c")).thenReturn(indexedTypeC);
	}

	@Test
	public void verifyIndexerIsCreated() throws Exception {
		CxIndexer searchIndexer = defaultCxIndexerService.getSearchIndexer("searchConfig", "type-a");
		assertThat(searchIndexer).isNotNull();
	}

	@Test
	public void verifyIndexerAddsItemToIndex() throws Exception {
		ItemModel item = modelService.create(ItemModel.class);
		modelService.save(item);

		defaultCxIndexerService.addItemToIndex("searchConfig", "type-a", item);
		verify(indexerServiceMock).updateTypeIndex(facetSearchConfig, indexConfig.getIndexedTypes().get("type-a"), List.of(item.getPk()));
	}

	@Test
	public void verifyIndexerAddsItemsToIndex() throws Exception {
		ItemModel item1 = modelService.create(ItemModel.class);
		ItemModel item2 = modelService.create(ItemModel.class);
		modelService.saveAll(item1, item2);

		defaultCxIndexerService.addItemsToIndex("searchConfig", "type-a", List.of(item1, item2));
		verify(indexerServiceMock).updateTypeIndex(facetSearchConfig, indexConfig.getIndexedTypes().get("type-a"), List.of(item1.getPk(), item2.getPk()));
	}

	@Test
	public void verifyIndexerAddsItemsByQueryToIndex() throws Exception {
		CxIndexer searchIndexer = defaultCxIndexerService.getSearchIndexer("searchConfig", "type-a");
		searchIndexer.addItemsByQuery();
		verify(indexerServiceMock).updateTypeIndex(facetSearchConfig, indexConfig.getIndexedTypes().get("type-a"));
	}

	@Test
	public void verifyIndexerRemovesItemFromIndex() throws Exception {
		ItemModel item = modelService.create(ItemModel.class);
		modelService.save(item);

		defaultCxIndexerService.removeItemFromIndex("searchConfig", "type-a", item);
		verify(indexerServiceMock).deleteTypeIndex(facetSearchConfig, indexConfig.getIndexedTypes().get("type-a"), List.of(item.getPk()));
	}

	@Test
	public void verifyIndexerRemovesItemsFromIndex() throws Exception {
		ItemModel item1 = modelService.create(ItemModel.class);
		ItemModel item2 = modelService.create(ItemModel.class);
		modelService.saveAll(item1, item2);

		defaultCxIndexerService.removeItemsFromIndex("searchConfig", "type-a", List.of(item1, item2));
		verify(indexerServiceMock).deleteTypeIndex(facetSearchConfig, indexConfig.getIndexedTypes().get("type-a"), List.of(item1.getPk(), item2.getPk()));
	}

	@Test
	public void verifyIndexerRemovesItemsByQueryFromIndex() throws Exception {
		CxIndexer searchIndexer = defaultCxIndexerService.getSearchIndexer("searchConfig", "type-a");
		searchIndexer.removeItemsByQuery();
		verify(indexerServiceMock).deleteTypeIndex(facetSearchConfig, indexConfig.getIndexedTypes().get("type-a"));
	}

	@Test
	public void verifyPerformIndexForIndexedTypes() throws IndexerException {
		when(indexerStrategyFactoryMock.createIndexerStrategy(facetSearchConfig)).thenReturn(indexerStrategyMock);

		defaultCxIndexerService.performIndexForIndexedTypes(facetSearchConfig, IndexOperation.FULL, List.of("type-a", "type-b"));
		verify(indexerStrategyMock, times(2)).execute();
	}
}
