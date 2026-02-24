package me.cxdev.commerce.toolkit.testing.testdoubles.user;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ThreadFactory;

import de.hybris.platform.cache.Cache;
import de.hybris.platform.cache.InvalidationManager;
import de.hybris.platform.core.Tenant;
import de.hybris.platform.jalo.JaloConnection;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.extension.ItemLifecycleListener;
import de.hybris.platform.jalo.security.JaloSecurityException;
import de.hybris.platform.jdbcwrapper.HybrisDataSource;
import de.hybris.platform.persistence.SystemEJB;
import de.hybris.platform.persistence.framework.PersistencePool;
import de.hybris.platform.persistence.numberseries.SerialNumberGenerator;
import de.hybris.platform.persistence.property.PersistenceManager;
import de.hybris.platform.util.SingletonCreator;
import de.hybris.platform.util.config.ConfigIntf;
import de.hybris.platform.util.config.FastHashMapConfig;
import de.hybris.platform.util.threadpool.ThreadPool;

public class TenantDummy implements Tenant {
	public static Tenant dummy(String tenantId) {
		return new TenantDummy(tenantId);
	}

	public static Tenant withConfig(String tenantId, Map<String, String> configParameters) {
		return new TenantDummy(tenantId, configParameters);
	}

	private String tenantId;
	private ConfigIntf config;
	private HybrisDataSource hybrisDataSource;

	private TenantDummy(String tenantId) {
		this(tenantId, Map.of());
	}

	private TenantDummy(String tenantId, Map<String, String> configParameters) {
		this.tenantId = tenantId;
		this.config = new FastHashMapConfig(configParameters);
	}

	@Override
	public List<ItemLifecycleListener> getAllItemLifecycleListeners() {
		return List.of();
	}

	@Override
	public List<String> getTenantSpecificExtensionNames() {
		return List.of();
	}

	@Override
	public ConfigIntf getConfig() {
		return config;
	}

	@Override
	public String getTenantID() {
		return tenantId;
	}

	@Override
	public Locale getTenantSpecificLocale() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public TimeZone getTenantSpecificTimeZone() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public InvalidationManager getInvalidationManager() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Cache getCache() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public PersistencePool getPersistencePool() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public PersistenceManager getPersistenceManager() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public SystemEJB getSystemEJB() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public ThreadPool getThreadPool() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public ThreadPool getWorkersThreadPool() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public SingletonCreator getSingletonCreator() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public JaloConnection getJaloConnection() {
		JaloConnection jaloConnection = mock(JaloConnection.class);
		try {
			when(jaloConnection.createAnonymousCustomerSession()).thenReturn(mock(JaloSession.class));
		} catch (JaloSecurityException e) {}
		return jaloConnection;

	}

	@Override
	public SerialNumberGenerator getSerialNumberGenerator() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public JaloSession getActiveSession() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public void resetTenantRestartMarker() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Thread createAndRegisterBackgroundThread(Runnable runnable, ThreadFactory threadFactory) {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public HybrisDataSource getDataSource(String paramString) {
		return getDataSource();
	}

	@Override
	public HybrisDataSource getMasterDataSource() {
		return getDataSource();
	}

	@Override
	public HybrisDataSource getDataSource() {
		if (hybrisDataSource == null) {
			throw new UnsupportedOperationException("not supported for testing");
		}
		return this.hybrisDataSource;
	}

	@Override
	public boolean isSlaveDataSource() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public boolean isAlternativeMasterDataSource() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public boolean isForceMaster() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public String activateSlaveDataSource() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public void activateSlaveDataSource(String paramString) {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public void activateAlternativeMasterDataSource(String paramString) {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Collection<HybrisDataSource> getAllSlaveDataSources() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Collection<HybrisDataSource> getAllAlternativeMasterDataSources() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Set<String> getAllDataSourceIDs() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Set<String> getAllSlaveDataSourceIDs() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public Set<String> getAllAlternativeMasterDataSourceIDs() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public void deactivateSlaveDataSource() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public void deactivateAlternativeDataSource() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	@Override
	public void forceMasterDataSource() {
		throw new UnsupportedOperationException("not supported for testing");
	}

	public void setHybrisDataSource(HybrisDataSource hybrisDataSource) {
		this.hybrisDataSource = hybrisDataSource;
	}
}
