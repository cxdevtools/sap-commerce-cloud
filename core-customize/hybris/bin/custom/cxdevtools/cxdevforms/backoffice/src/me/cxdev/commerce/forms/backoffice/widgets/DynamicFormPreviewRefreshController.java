package me.cxdev.commerce.forms.backoffice.widgets;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.annotations.GlobalCockpitEvent;
import com.hybris.cockpitng.common.model.ObjectWithComponentContext;
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
        if (wizardResult != null && wizardResult.get("newObject") instanceof DynamicFormFieldModel field) {
            refreshForm(field);
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
        if (updatedItems != null) {
            updatedItems.stream().filter(DynamicFormFieldModel.class::isInstance)
                    .map(DynamicFormFieldModel.class::cast).findFirst().ifPresent(this::refreshForm);
        }
    }

    private void refreshForm(final DynamicFormFieldModel field) {
        if (field.getForm() != null) {
            // inputObject rerenders the current form but retains it as the selected object.
            // The wrapper makes the Editor Area process the reload even though it is the same model PK.
            sendOutput("refresh", new ObjectWithComponentContext(field.getForm()));
        }
    }
}
