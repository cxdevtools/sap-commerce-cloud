package me.cxdev.commerce.toolkit.impex;

import de.hybris.platform.catalog.CatalogVersionService;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.catalog.model.SyncItemJobModel;
import de.hybris.platform.catalog.synchronization.CatalogSynchronizationService;
import de.hybris.platform.catalog.synchronization.SyncConfig;
import de.hybris.platform.core.Registry;
import de.hybris.platform.cronjob.enums.ErrorMode;
import de.hybris.platform.cronjob.enums.JobLogLevel;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.servicelayer.cronjob.CronJobService;
import de.hybris.platform.servicelayer.exceptions.SystemException;

import org.apache.commons.lang3.StringUtils;

/**
 * Util class that provides useful execution tasks for ImpEx imports.
 */
public final class ImpExTasks {
	private ImpExTasks() {
		// Avoid instantiation
	}

	/**
	 * This method is intended to be called by an ImpEx script while code execution is enabled, e.g.:
	 *
	 * <code>
	 * #%me.cxdev.commerce.toolkit.impex.ImpExTasks.startFullCatalogSync("ProductCatalog:Staged", "ProductCatalog:Online", "sync (ProductCatalog:Online->Staged)")
	 * </code>
	 *
	 * One may use it to sync the catalog versions after system update. The action is performed synchronously, so that
	 * you can rely on the action was completed before your system update process continues.
	 *
	 * @param source      the cron job to be executed (syntax {catalogid}:{catalogversion})
	 * @param target      the target catalog version (syntax {catalogid}:{catalogversion})
	 * @param syncJobCode the sync job code to be executed
	 */
	public static void startFullCatalogSync(String source, String target, String syncJobCode) {
		CatalogVersionService versionService = (CatalogVersionService) Registry.getApplicationContext().getBean("catalogVersionService");
		CatalogSynchronizationService synchronizationService = (CatalogSynchronizationService) Registry.getApplicationContext().getBean("catalogSynchronizationService");
		try {
			CatalogVersionModel sourceCatalogVersion = versionService.getCatalogVersion(StringUtils.substringBefore(source, ":"), StringUtils.substringAfter(source, ":"));
			CatalogVersionModel targetCatalogVersion = versionService.getCatalogVersion(StringUtils.substringBefore(target, ":"), StringUtils.substringAfter(target, ":"));
			SyncItemJobModel syncJob = synchronizationService.getSyncJob(sourceCatalogVersion, targetCatalogVersion, syncJobCode);
			SyncConfig syncConfig = new SyncConfig();
			syncConfig.setSynchronous(true);
			syncConfig.setKeepCronJob(false);
			syncConfig.setForceUpdate(Boolean.FALSE);
			syncConfig.setCreateSavedValues(Boolean.FALSE);
			syncConfig.setLogToFile(Boolean.FALSE);
			syncConfig.setLogLevelFile(JobLogLevel.INFO);
			syncConfig.setLogToDatabase(Boolean.FALSE);
			syncConfig.setLogLevelDatabase(JobLogLevel.WARNING);
			syncConfig.setErrorMode(ErrorMode.IGNORE);
			syncConfig.setAbortWhenCollidingSyncIsRunning(false);
			synchronizationService.synchronize(syncJob, syncConfig);
		} catch (Exception e) {
			throw new SystemException(String.format(
					"Could not start full catalog sync for parameters [source=%s, target=%s, code=%s]! Please verify the catalog versions are valid and the sync job is configured correctly.",
					source, target, syncJobCode), e);
		}
	}

	/**
	 * This method is intended to be called by an ImpEx script while code execution is enabled, e.g.:
	 *
	 * <code>#%me.cxdev.commerce.toolkit.impex.ImpExTasks.startCronJob("your-cronjob-code");</code>
	 *
	 * One may use it to e.g. trigger a solr indexing after system update. The action is performed synchronously, so that you can rely on
	 * the action was completed before your system update process continues.
	 *
	 * @param cronJobCode the cron job code to be executed
	 */
	public static void startCronJob(String cronJobCode) {
		startCronJob(cronJobCode, true);
	}

	/**
	 * This method is intended to be called by an ImpEx script while code execution is enabled, e.g.:
	 *
	 * <code>#%me.cxdev.commerce.toolkit.impex.ImpExTasks.startCronJobNonBlocking("your-cronjob-code");</code>
	 *
	 * One may use it to e.g. trigger a solr indexing after system update. The action is performed asynchronously. You cannot rely on
	 * the action being completed before your system update process continues.
	 *
	 * @param cronJobCode the cron job code to be executed
	 */
	public static void startCronJobNonBlocking(String cronJobCode) {
		startCronJob(cronJobCode, false);
	}

	/**
	 * This method is intended to be called by an ImpEx script while code execution is enabled, e.g.:
	 *
	 * <code>#%me.cxdev.commerce.toolkit.impex.ImpExTasks.startCronJob("your-cronjob-code", true);</code>
	 *
	 * One may use it to e.g. trigger a solr indexing after system update. The second parameter defines whether the job
	 * is performed synchronously (true) or asynchronously (false).
	 *
	 * @param cronJobCode the cron job code to be executed
	 * @param synchronous perform cronjob synchronously (true) or asynchronously (false)
	 */
	public static void startCronJob(String cronJobCode, boolean synchronous) {
		CronJobService cronJobService = (CronJobService) Registry.getApplicationContext().getBean("cronJobService");
		try {
			CronJobModel cronJob = cronJobService.getCronJob(cronJobCode);
			cronJobService.performCronJob(cronJob, synchronous);
		} catch (Exception e) {
			throw new SystemException(String.format("Could not start cron job for parameters [code=%s]! Please verify the cron job is configured correctly.", cronJobCode), e);
		}
	}
}
