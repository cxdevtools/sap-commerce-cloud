package me.cxdev.commerce.toolkit.testing.testdoubles.catalog;

import static me.cxdev.commerce.toolkit.testing.itemmodel.InMemoryModelFactory.createTestableItemModel;

import java.util.Collection;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.DuplicatedItemIdentifier;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.security.PrincipalModel;
import de.hybris.platform.core.model.user.UserModel;

public class CatalogVersionServiceFake implements CatalogVersionService {
	private Collection<CatalogVersionModel> readableCatalogVersions;

	@Override
	public void setSessionCatalogVersion(String catalogId, String catalogVersionName) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public void addSessionCatalogVersion(CatalogVersionModel catalogVersion) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public Collection<CatalogVersionModel> getSessionCatalogVersions() {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public void setSessionCatalogVersions(Collection<CatalogVersionModel> catalogVersions) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public CatalogVersionModel getCatalogVersion(String catalogId, String catalogVersionName) {
		CatalogModel catalog = createTestableItemModel(CatalogModel.class);
		catalog.setId(catalogId);
		CatalogVersionModel catalogVersion = createTestableItemModel(CatalogVersionModel.class);
		catalogVersion.setVersion(catalogVersionName);
		catalogVersion.setCatalog(catalog);
		return catalogVersion;
	}

	@Override
	public CatalogVersionModel getSessionCatalogVersionForCatalog(String catalogId) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public Collection<CatalogVersionModel> getSessionCatalogVersionsForCatalog(String catalogId) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public boolean canRead(CatalogVersionModel catalogVersion, UserModel user) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public boolean canWrite(CatalogVersionModel catalogVersion, UserModel user) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public Collection<CatalogVersionModel> getAllWritableCatalogVersions(PrincipalModel principal) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public Collection<CatalogVersionModel> getAllReadableCatalogVersions(PrincipalModel principal) {
		if (this.readableCatalogVersions == null) {
			throw new UnsupportedOperationException("This method is not supported with testing");
		}
		return this.readableCatalogVersions;
	}

	@Override
	public Collection<CatalogVersionModel> getAllCatalogVersions() {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public <T extends CatalogVersionModel> Collection<T> getAllCatalogVersionsOfType(Class<T> versionType) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	@Override
	public Collection<DuplicatedItemIdentifier> findDuplicatedIds(CatalogVersionModel catalogVersionModel) {
		throw new UnsupportedOperationException("This method is not supported with testing");
	}

	public void setReadableCatalogVersions(Collection<CatalogVersionModel> readableCatalogVersions) {
		this.readableCatalogVersions = readableCatalogVersions;
	}
}
