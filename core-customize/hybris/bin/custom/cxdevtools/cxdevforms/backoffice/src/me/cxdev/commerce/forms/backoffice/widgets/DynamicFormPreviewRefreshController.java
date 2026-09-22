package me.cxdev.commerce.forms.backoffice.widgets;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.core.events.CockpitEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

import java.util.Collection;
import java.util.Map;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;

/**
 * Refreshes the DynamicForm editor after a field was created or saved through the
 * standard Backoffice editor.
 */
public class DynamicFormPreviewRefreshController extends DefaultWidgetController {

    @SocketEvent(socketId = "wizardResult")
    public void handleWizardResult(final Map<String, Object> wizardResult) {
        if (wizardResult != null && wizardResult.get("newObject") instanceof DynamicFormFieldModel) {
            sendOutput("refresh", Boolean.TRUE);
        }
    }

    /**
     * The standard editor publishes {@code objectsUpdated} after it has persisted
     * an item. This also works for the multi-item collection dialog used here,
     * whose single-item return socket is intentionally not emitted.
     */
    @GlobalCockpitEvent(eventName = "objectsUpdated", scope = "session")
    public void handleObjectUpdated(final CockpitEvent event) {
        final Collection<?> updatedItems = event == null ? null : event.getDataAsCollection();
        if (updatedItems != null && updatedItems.stream().anyMatch(DynamicFormFieldModel.class::isInstance)) {
            sendOutput("refresh", Boolean.TRUE);
        }
    }
}
