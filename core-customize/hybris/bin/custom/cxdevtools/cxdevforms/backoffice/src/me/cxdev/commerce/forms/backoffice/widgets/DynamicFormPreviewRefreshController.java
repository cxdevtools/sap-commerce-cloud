package me.cxdev.commerce.forms.backoffice.widgets;

import com.hybris.cockpitng.annotations.SocketEvent;
import com.hybris.cockpitng.util.DefaultWidgetController;

import java.util.Map;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;

/** Forwards only DynamicFormField wizard results to the current editor-area refresh. */
public class DynamicFormPreviewRefreshController extends DefaultWidgetController {

    @SocketEvent(socketId = "wizardResult")
    public void handleWizardResult(final Map<String, Object> wizardResult) {
        if (wizardResult != null && wizardResult.get("newObject") instanceof DynamicFormFieldModel) {
            sendOutput("refresh", Boolean.TRUE);
        }
    }
}
