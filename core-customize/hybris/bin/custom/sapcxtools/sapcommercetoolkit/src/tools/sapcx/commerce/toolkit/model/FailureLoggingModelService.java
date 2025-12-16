package tools.sapcx.commerce.toolkit.model;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.exceptions.ModelInitializationException;
import de.hybris.platform.servicelayer.exceptions.ModelRemovalException;
import de.hybris.platform.servicelayer.exceptions.ModelSavingException;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.servicelayer.internal.model.ModelCloningContext;
import de.hybris.platform.servicelayer.internal.model.impl.DefaultModelService;
import de.hybris.platform.servicelayer.model.ModelService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the {@link ModelService} interface add enhances the
 * logging capabilities to trace errors within calls to the model service.
 * <p>
 * This is especially helpful when debugging catalog synchronization issues,
 * as the {@link de.hybris.platform.servicelayer.internal.model.impl.DefaultModelService}
 * does not log out any exceptions and the synchronization workers also do not
 * print out the exception, but only a generic error message.
 * <p>
 * Note: This class was originally designed as a delegate to the
 * {@link de.hybris.platform.servicelayer.model.ModelService} interface, but
 * unfortunately, there is an outdated SAP implementation of this interface
 * (com.sap.hybris.sapcustomerb2b.outbound.SAPB2BModelService) within the
 * sapcustomerb2b extension, which requires the bean alias "modelService"
 * to be a subtype of the {@link DefaultModelService} class.
 * <p>
 * This class will be refactored to the delegate pattern, once this unfortunate
 * implementation class is removed from the platform.
 */
public class FailureLoggingModelService extends DefaultModelService {
	private static final Logger LOG = LoggerFactory.getLogger(FailureLoggingModelService.class);
	private static final String EXCEPTION_MESSAGE = "Exception of type '%s' occurred during model service interaction: %s";
	private static final String SUPPRESSED_EXCEPTION_MESSAGE = "Suppressed exception (%d of %d): %s";

	private void wrapWithLoggingCapabilities(final Runnable command) {
		try {
			command.run();
		} catch (IllegalArgumentException | SystemException | NullPointerException e) {
			performLogging(e);
			throw e;
		}
	}

	private <T> T wrapWithLoggingCapabilities(final Supplier<T> command) {
		try {
			return command.get();
		} catch (IllegalArgumentException | SystemException | NullPointerException e) {
			performLogging(e);
			throw e;
		}
	}

	private <T extends RuntimeException> void performLogging(final T exception) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format(EXCEPTION_MESSAGE, exception.getClass().getSimpleName(), exception.getLocalizedMessage()), exception);

			final Throwable[] suppressed = exception.getSuppressed();
			if (suppressed != null) {
				int total = suppressed.length;
				for (int i = 0; i < total; i++) {
					LOG.debug(String.format(SUPPRESSED_EXCEPTION_MESSAGE, i, total, suppressed[i].getLocalizedMessage()), suppressed[i]);
				}
			}
		}
	}

	@Override
	public void attach(Object o) {
		wrapWithLoggingCapabilities(() -> super.attach(o));
	}

	@Override
	public void detach(Object o) {
		wrapWithLoggingCapabilities(() -> super.detach(o));
	}

	@Override
	public void detach(PK pk) {
		wrapWithLoggingCapabilities(() -> super.detach(pk));
	}

	@Override
	public void detachAll() {
		wrapWithLoggingCapabilities(() -> super.detachAll());
	}

	@Override
	public <T> T clone(T t) {
		return wrapWithLoggingCapabilities(() -> super.clone(t));
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass) {
		return wrapWithLoggingCapabilities(() -> super.clone(o, aClass));
	}

	@Override
	public <T> T clone(T t, ModelCloningContext modelCloningContext) {
		return wrapWithLoggingCapabilities(() -> super.clone(t, modelCloningContext));
	}

	@Override
	public <T> T clone(Object o, Class<T> aClass, ModelCloningContext modelCloningContext) {
		return wrapWithLoggingCapabilities(() -> super.clone(o, aClass, modelCloningContext));
	}

	@Override
	public <T> T create(Class aClass) {
		return wrapWithLoggingCapabilities(() -> super.create(aClass));
	}

	@Override
	public <T> T create(String s) {
		return wrapWithLoggingCapabilities(() -> super.create(s));
	}

	@Override
	public <T> T get(Object o) {
		return wrapWithLoggingCapabilities(() -> super.get(o));
	}

	@Override
	public <T> T get(Object o, String s) {
		return wrapWithLoggingCapabilities(() -> super.get(o, s));
	}

	@Override
	public <T> T get(PK pk) {
		return wrapWithLoggingCapabilities(() -> super.get(pk));
	}

	@Override
	public <T extends Collection> T getAll(Collection<?> collection, T t) {
		return wrapWithLoggingCapabilities(() -> super.getAll(collection, t));
	}

	@Override
	public <T extends Collection> T getAll(Collection<?> collection, T t, String s) {
		return wrapWithLoggingCapabilities(() -> super.getAll(collection, t, s));
	}

	@Override
	public void refresh(Object o) {
		wrapWithLoggingCapabilities(() -> super.refresh(o));
	}

	@Override
	public void save(Object o) throws ModelSavingException {
		wrapWithLoggingCapabilities(() -> super.save(o));
	}

	@Override
	public void saveAll(Collection<?> collection) throws ModelSavingException {
		wrapWithLoggingCapabilities(() -> super.saveAll(collection));
	}

	@Override
	public void saveAll(Object... objects) throws ModelSavingException {
		wrapWithLoggingCapabilities(() -> super.saveAll(objects));
	}

	@Override
	public void saveAll() throws ModelSavingException {
		wrapWithLoggingCapabilities(() -> super.saveAll());
	}

	@Override
	public boolean isUniqueConstraintErrorAsRootCause(Exception e) {
		return wrapWithLoggingCapabilities(() -> super.isUniqueConstraintErrorAsRootCause(e));
	}

	@Override
	public void remove(Object o) throws ModelRemovalException {
		wrapWithLoggingCapabilities(() -> super.remove(o));
	}

	@Override
	public void removeAll(Collection<?> collection) throws ModelRemovalException {
		wrapWithLoggingCapabilities(() -> super.removeAll(collection));
	}

	@Override
	public void removeAll(Object... objects) throws ModelRemovalException {
		wrapWithLoggingCapabilities(() -> super.removeAll(objects));
	}

	@Override
	public void remove(PK pk) throws ModelRemovalException {
		wrapWithLoggingCapabilities(() -> super.remove(pk));
	}

	@Override
	public <T> T getSource(Object o) {
		return wrapWithLoggingCapabilities(() -> super.getSource(o));
	}

	@Override
	public <T extends Collection> T getAllSources(Collection<?> collection, T t) {
		return wrapWithLoggingCapabilities(() -> super.getAllSources(collection, t));
	}

	@Override
	public String getModelType(Class aClass) {
		return wrapWithLoggingCapabilities(() -> super.getModelType(aClass));
	}

	@Override
	public Class getModelTypeClass(Class aClass) {
		return wrapWithLoggingCapabilities(() -> super.getModelTypeClass(aClass));
	}

	@Override
	public String getModelType(Object o) {
		return wrapWithLoggingCapabilities(() -> super.getModelType(o));
	}

	@Override
	public <T> T toModelLayer(Object o) {
		return wrapWithLoggingCapabilities(() -> super.toModelLayer(o));
	}

	@Override
	public <T> T toPersistenceLayer(Object o) {
		return wrapWithLoggingCapabilities(() -> super.toPersistenceLayer(o));
	}

	@Override
	public void initDefaults(Object o) throws ModelInitializationException {
		wrapWithLoggingCapabilities(() -> super.initDefaults(o));
	}

	@Override
	public <T> T getAttributeValue(Object o, String s) {
		return wrapWithLoggingCapabilities(() -> super.getAttributeValue(o, s));
	}

	@Override
	public <T> T getAttributeValue(Object o, String s, Locale locale) {
		return wrapWithLoggingCapabilities(() -> super.getAttributeValue(o, s, locale));
	}

	@Override
	public <T> Map<Locale, T> getAttributeValues(Object o, String s, Locale... locales) {
		return wrapWithLoggingCapabilities(() -> super.getAttributeValues(o, s, locales));
	}

	@Override
	public void setAttributeValue(Object o, String s, Object o1) {
		wrapWithLoggingCapabilities(() -> super.setAttributeValue(o, s, o1));
	}

	@Override
	public <T> void setAttributeValue(Object o, String s, Map<Locale, T> map) {
		wrapWithLoggingCapabilities(() -> super.setAttributeValue(o, s, map));
	}

	@Override
	public boolean isUpToDate(Object o) {
		return wrapWithLoggingCapabilities(() -> super.isUpToDate(o));
	}

	@Override
	public boolean isModified(Object o) {
		return wrapWithLoggingCapabilities(() -> super.isModified(o));
	}

	@Override
	public boolean isNew(Object o) {
		return wrapWithLoggingCapabilities(() -> super.isNew(o));
	}

	@Override
	public boolean isRemoved(Object o) {
		return wrapWithLoggingCapabilities(() -> super.isRemoved(o));
	}

	@Override
	public boolean isAttached(Object o) {
		return wrapWithLoggingCapabilities(() -> super.isAttached(o));
	}

	@Override
	public boolean isSourceAttached(Object o) {
		return wrapWithLoggingCapabilities(() -> super.isSourceAttached(o));
	}

	@Override
	public void enableTransactions() {
		wrapWithLoggingCapabilities(() -> super.enableTransactions());
	}

	@Override
	public void disableTransactions() {
		wrapWithLoggingCapabilities(() -> super.disableTransactions());
	}

	@Override
	public void clearTransactionsSettings() {
		wrapWithLoggingCapabilities(() -> super.clearTransactionsSettings());
	}

	@Override
	public void lock(PK pk) {
		wrapWithLoggingCapabilities(() -> super.lock(pk));
	}

	@Override
	public void lock(Object o) {
		wrapWithLoggingCapabilities(() -> super.lock(o));
	}

	@Override
	public <T> T getWithLock(Object o) {
		return wrapWithLoggingCapabilities(() -> super.getWithLock(o));
	}
}
