package me.cxdev.commerce.forms.backoffice.renderers;

import static java.util.stream.Collectors.joining;

import de.hybris.platform.servicelayer.model.ModelService;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractSection;
import com.hybris.cockpitng.components.Action;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionDefinition;
import com.hybris.cockpitng.actions.ActionListener;
import com.hybris.cockpitng.actions.CockpitAction;
import com.hybris.cockpitng.actions.impl.DefaultActionRenderer;
import com.hybris.cockpitng.data.TypeAwareSelectionContext;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.dataaccess.facades.permissions.PermissionFacade;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.HtmlNativeComponent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import me.cxdev.commerce.forms.enums.DynamicFormFieldType;
import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;
import me.cxdev.commerce.forms.model.DynamicFormStepModel;

/**
 * Interactive, non-submittable preview of a form definition in Backoffice.
 * Fields, including those only visible for a given field value, can be reordered with
 * drag-and-drop or the accessible move buttons. Moving a field next to a field of another
 * step reassigns it to that step.
 */
public class DynamicFormPreviewRenderer implements WidgetComponentRenderer<Component, AbstractSection, Object> {

    private static final String FIELD_ATTRIBUTE = "cxdevforms.preview.field";
    private static final String EDIT_FIELD_OUTPUT = "cxdevformsEditField";
    private static final String CREATE_FIELD_OUTPUT = "cxdevformsCreateField";
    private static final String ACTION_SLOT_STYLE = "display: inline-flex; align-items: center; justify-content: center;"
            + " flex: 0 0 28px; width: 28px; min-width: 28px; max-width: 28px; height: 28px; min-height: 28px;"
            + " max-height: 28px; box-sizing: border-box; margin: 0;";
    private final ModelService modelService;
    private final PermissionFacade permissionFacade;

    public DynamicFormPreviewRenderer(final ModelService modelService, final PermissionFacade permissionFacade) {
        this.modelService = modelService;
        this.permissionFacade = permissionFacade;
    }

    @Override
    public void render(final Component parent, final AbstractSection configuration, final Object object,
            final DataType dataType, final WidgetInstanceManager widgetInstanceManager) {
        if (!(object instanceof DynamicFormModel form)) {
            return;
        }
        renderPreview(parent, form, widgetInstanceManager);
    }

    private void renderPreview(final Component parent, final DynamicFormModel form,
            final WidgetInstanceManager widgetInstanceManager) {
        parent.getChildren().clear();
        final List<DynamicFormFieldModel> rootFields = form.getFormFields().stream()
                .filter(field -> field.getParentFieldValue() == null)
                .toList();
        if (rootFields.isEmpty()) {
            final Vlayout emptyState = new Vlayout();
            emptyState.setSpacing("8px");
            emptyState.appendChild(new Label(label("cxdevforms.preview.empty")));
            appendAddFieldButton(emptyState, form, widgetInstanceManager);
            parent.appendChild(emptyState);
            return;
        }
        final Vlayout preview = new Vlayout();
        preview.setSpacing("0");
        preview.setWidth("100%");
        preview.setStyle("width: 100%; background: #ffffff; border: 1px solid #d9d9d9; box-sizing: border-box;");
        final Set<DynamicFormFieldModel> visibleFields = collectVisibleFields(rootFields);
        final List<Action> deleteActions = new ArrayList<>();
        final List<DynamicFormFieldModel> orderedFields = new ArrayList<>(form.getFormFields());
        // The render order (not just the root fields) is what "move up/down" and drag-and-drop
        // operate on, so that conditional fields can be reordered like any other field.
        final List<DynamicFormFieldModel> renderOrder = computeRenderOrder(form, orderedFields, visibleFields);
        final Map<DynamicFormFieldModel, Integer> positions = new HashMap<>();
        for (int i = 0; i < renderOrder.size(); i++) {
            positions.put(renderOrder.get(i), i);
        }
        final Set<DynamicFormFieldModel> rendered = new HashSet<>();
        for (final DynamicFormFieldModel field : orderedFields) {
            if (field.getStep() == null && visibleFields.contains(field) && rendered.add(field)) {
                preview.appendChild(renderField(form, renderOrder, field, positions.get(field), deleteActions,
                        widgetInstanceManager, parent));
            }
        }
        for (final DynamicFormStepModel step : form.getSteps()) {
            final List<DynamicFormFieldModel> fieldsForStep = orderedFields.stream()
                    .filter(field -> Objects.equals(field.getStep(), step) && visibleFields.contains(field))
                    .filter(rendered::add).toList();
            if (!fieldsForStep.isEmpty()) {
                preview.appendChild(renderStepHeading(step));
                for (final DynamicFormFieldModel field : fieldsForStep) {
                    preview.appendChild(renderField(form, renderOrder, field, positions.get(field), deleteActions,
                            widgetInstanceManager, parent));
                }
            }
        }
        // Old or corrupt definitions can reference a removed step. Show them rather than hiding data.
        for (final DynamicFormFieldModel field : orderedFields) {
            if (visibleFields.contains(field) && rendered.add(field)) {
                preview.appendChild(renderField(form, renderOrder, field, positions.get(field), deleteActions,
                        widgetInstanceManager, parent));
            }
        }
        parent.appendChild(preview);
        // Actions created programmatically do not receive ZK's composition callback automatically.
        // The standard Backoffice Delete action is initialized here only after it belongs to the page.
        deleteActions.forEach(Action::afterCompose);
        appendAddFieldButton(parent, form, widgetInstanceManager);
    }

    /** Mirrors the field grouping/ordering rendered above, as a flat list usable for move positions. */
    private List<DynamicFormFieldModel> computeRenderOrder(final DynamicFormModel form,
            final List<DynamicFormFieldModel> orderedFields, final Set<DynamicFormFieldModel> visibleFields) {
        final List<DynamicFormFieldModel> renderOrder = new ArrayList<>();
        final Set<DynamicFormFieldModel> seen = new HashSet<>();
        for (final DynamicFormFieldModel field : orderedFields) {
            if (field.getStep() == null && visibleFields.contains(field) && seen.add(field)) {
                renderOrder.add(field);
            }
        }
        for (final DynamicFormStepModel step : form.getSteps()) {
            for (final DynamicFormFieldModel field : orderedFields) {
                if (Objects.equals(field.getStep(), step) && visibleFields.contains(field) && seen.add(field)) {
                    renderOrder.add(field);
                }
            }
        }
        for (final DynamicFormFieldModel field : orderedFields) {
            if (visibleFields.contains(field) && seen.add(field)) {
                renderOrder.add(field);
            }
        }
        return renderOrder;
    }

    private void appendAddFieldButton(final Component parent, final DynamicFormModel form,
            final WidgetInstanceManager widgetInstanceManager) {
        final Button addField = new Button(label("cxdevforms.preview.addField"));
        addField.setStyle("margin-top: 12px;");
        final boolean canAddField = permissionFacade.canCreateTypeInstance(DynamicFormFieldModel._TYPECODE)
                && canChangeFormFields(form);
        addField.setDisabled(!canAddField);
        if (canAddField) {
            addField.addEventListener(Events.ON_CLICK, event -> openCreateFieldWizard(form, widgetInstanceManager));
        }
        parent.appendChild(addField);
    }

    private Component renderField(final DynamicFormModel form, final List<DynamicFormFieldModel> renderOrder,
            final DynamicFormFieldModel field, final int position, final List<Action> deleteActions,
            final WidgetInstanceManager widgetInstanceManager,
            final Component previewParent) {
        final Vlayout fieldGroup = new Vlayout();
        fieldGroup.setSpacing("0");
        fieldGroup.setWidth("100%");
        final Div row = new Div();
        row.setAttribute(FIELD_ATTRIBUTE, field);
        row.setWidth("100%");
        row.setStyle("display: grid; grid-template-columns: 175px 300px 500px; justify-content: start;"
                + " gap: 0; align-items: start; padding: 12px; width: 100%; min-width: 0; box-sizing: border-box; border-bottom: 1px solid #e5e5e5;"
                + (Boolean.TRUE.equals(field.isHidden()) ? " opacity: 0.58;" : ""));
        final boolean canMoveFields = canChangeFormFields(form);
        if (canMoveFields) {
            row.setDraggable("true");
            row.setDroppable("true");
            row.addEventListener(Events.ON_DROP, event -> moveDroppedField(form, renderOrder, field, (DropEvent) event,
                    widgetInstanceManager, previewParent));
        }

        final Div labelCell = new Div();
        labelCell.setWidth("300px");
        labelCell.setStyle("width: 300px; min-width: 0; overflow-wrap: anywhere; box-sizing: border-box;");
        final Hlayout heading = new Hlayout();
        heading.setSpacing("6px");
        final Label title = new Label(displayName(field));
        title.setStyle("font-weight: 600;");
        heading.appendChild(title);
        if (Boolean.TRUE.equals(field.isRequired())) {
            final Label required = new Label("*");
            required.setStyle("color: #bb0000; font-weight: bold;");
            required.setTooltiptext(label("cxdevforms.preview.required"));
            heading.appendChild(required);
        }
        if (Boolean.TRUE.equals(field.isHidden())) {
            heading.appendChild(new Label("(" + label("cxdevforms.preview.hidden") + ")"));
        }
        labelCell.appendChild(heading);
        if (field.getDescription() != null && !field.getDescription().isBlank()) {
            final Label description = new Label(field.getDescription());
            description.setStyle("display: block; margin-top: 4px; color: #5b5b5b; white-space: pre-wrap;");
            labelCell.appendChild(description);
        }

        final Div inputCell = new Div();
        inputCell.setWidth("500px");
        inputCell.setStyle("min-width: 0; width: 500px; box-sizing: border-box;");
        inputCell.appendChild(renderInput(field));
        appendRule(inputCell, validationText(field));
        appendRule(inputCell, visibilityText(field));
        final Div actionsCell = new Div();
        actionsCell.setWidth("175px");
        actionsCell.setStyle("display: flex; align-items: center; justify-content: center; gap: 8px; flex: 0 0 auto;"
                + " width: 175px; min-width: 175px; max-width: 175px; padding-right: 8px;"
                + " white-space: nowrap; justify-self: start; box-sizing: border-box;");
        final Button edit = new Button();
        edit.setStyle(ACTION_SLOT_STYLE);
        edit.setIconSclass("z-icon-pencil");
        edit.setTooltiptext(label("cxdevforms.preview.edit"));
        edit.setAttribute("aria-label", label("cxdevforms.preview.edit"));
        final boolean canEditField = permissionFacade.canChangeInstance(field);
        edit.setDisabled(!canEditField);
        if (canEditField) {
            edit.addEventListener(Events.ON_CLICK, event -> openFieldEditor(widgetInstanceManager, form, field));
        }
        actionsCell.appendChild(edit);
        appendDeleteControl(actionsCell, field, form, deleteActions, widgetInstanceManager, previewParent);
        actionsCell.appendChild(moveButton(label("cxdevforms.preview.moveUp"), true,
                canMoveFields && position > 0,
                () -> moveField(form, renderOrder, field, position - 1, widgetInstanceManager, previewParent)));
        actionsCell.appendChild(moveButton(label("cxdevforms.preview.moveDown"), false,
                canMoveFields && position < renderOrder.size() - 1,
                () -> moveField(form, renderOrder, field, position + 1, widgetInstanceManager, previewParent)));
        row.appendChild(actionsCell);
        row.appendChild(labelCell);
        row.appendChild(inputCell);
        fieldGroup.appendChild(row);

        return fieldGroup;
    }

    private Component renderStepHeading(final DynamicFormStepModel step) {
        final Div heading = new Div();
        heading.setStyle("width: 100%; box-sizing: border-box; padding: 12px 12px 8px;"
                + " background: #f4f7fa; border-bottom: 1px solid #d9e1e8; color: #34495e; font-weight: 600;");
        heading.appendChild(new Label(displayName(step)));
        return heading;
    }

    private Set<DynamicFormFieldModel> collectVisibleFields(final List<DynamicFormFieldModel> rootFields) {
        final Set<DynamicFormFieldModel> visible = new HashSet<>();
        for (final DynamicFormFieldModel rootField : rootFields) {
            collectVisibleFields(rootField, visible, new HashSet<>());
        }
        return visible;
    }

    private void collectVisibleFields(final DynamicFormFieldModel field, final Set<DynamicFormFieldModel> visible,
            final Set<DynamicFormFieldModel> path) {
        if (!visible.add(field) || !path.add(field)) {
            return;
        }
        for (final DynamicFormFieldValueModel value : field.getFormFieldValues()) {
            if (isSelected(field, value)) {
                for (final DynamicFormFieldModel child : sorted(value.getChildFields())) {
                    collectVisibleFields(child, visible, new HashSet<>(path));
                }
            }
        }
    }

    private Button moveButton(final String tooltip, final boolean up, final boolean enabled, final Runnable action) {
        final Button button = new Button();
        button.setStyle(ACTION_SLOT_STYLE);
        button.setClass("cng-action-icon cng-font-icon font-icon--navigation-" + (up ? "up" : "down") + "-arrow z-button");
        button.setTooltiptext(tooltip);
        button.setDisabled(!enabled);
        if (enabled) {
            button.addEventListener(Events.ON_CLICK, event -> action.run());
        }
        return button;
    }

    private void appendDeleteControl(final Div actionsCell, final DynamicFormFieldModel field, final DynamicFormModel form,
            final List<Action> deleteActions, final WidgetInstanceManager widgetInstanceManager, final Component previewParent) {
        if (!permissionFacade.canRemoveInstance(field)) {
            final Button delete = new Button();
            delete.setStyle(ACTION_SLOT_STYLE);
            delete.setClass("cng-action-icon cng-font-icon font-icon--delete z-button");
            delete.setTooltiptext(label("cxdevforms.preview.delete"));
            delete.setDisabled(true);
            actionsCell.appendChild(delete);
            return;
        }

        // Keep the stock delete action and its stock confirmation renderer, but render its trigger
        // as the same plain ZK icon button used by the other preview actions.
        final Action delete = new PreviewDeleteAction(label("cxdevforms.preview.delete"));
        delete.setActionId("com.hybris.cockpitng.action.delete");
        delete.setWidgetInstanceManager(widgetInstanceManager);
        delete.setInputValue(List.of(field));
        delete.setViewMode(ActionContext.VIEWMODE_ICONONLY);
        delete.setStyle("display: inline-flex; flex: 0 0 28px; width: 28px; min-width: 28px; max-width: 28px;");
        delete.addEventListener(Action.ON_ACTION_PERFORMED, event -> {
            modelService.refresh(form);
            renderPreview(previewParent, form, widgetInstanceManager);
        });
        actionsCell.appendChild(delete);
        deleteActions.add(delete);
    }

    private boolean canChangeFormFields(final DynamicFormModel form) {
        return permissionFacade.canChangeInstance(form)
                && permissionFacade.canChangeInstanceProperty(form, DynamicFormModel.FORMFIELDS);
    }

    /**
     * Uses the stock delete action and its confirmation dialog, but prevents its success
     * socket from navigating away from the currently edited DynamicForm.
     */
    private static final class PreviewDeleteAction extends Action {
        private static final PreviewActionRenderer ACTION_RENDERER = new PreviewActionRenderer();
        private final String tooltip;

        private PreviewDeleteAction(final String tooltip) {
            this.tooltip = tooltip;
        }

        @Override
        @SuppressWarnings({"rawtypes", "unchecked"})
        protected void renderAction(final HtmlBasedComponent component, final ActionDefinition definition,
                final CockpitAction action, final boolean initialize) {
            final ActionContext context = createActionContext(definition);
            initializeActionContext(context, component, definition, action, initialize);

            final Button button = new Button();
            button.setStyle(ACTION_SLOT_STYLE);
            button.setClass("cng-action-icon cng-font-icon font-icon--delete z-button");
            button.setTooltiptext(tooltip);
            button.setAttribute("aria-label", tooltip);
            final boolean enabled = action.canPerform(context);
            button.setDisabled(!enabled);
            if (enabled) {
                final ActionListener listener = result -> Events.postEvent(Action.ON_ACTION_PERFORMED, this, result);
                button.addEventListener(Events.ON_CLICK,
                        event -> ACTION_RENDERER.performWithConfirmation(action, context, listener));
            }
            component.appendChild(button);
        }

        @Override
        protected void sendOutput(final String outputId, final Object output) {
            // The preview itself refreshes after a successful delete; the editor must stay open.
        }
    }

    /** Exposes the platform's confirmation-aware action execution without its table-based visual renderer. */
    private static final class PreviewActionRenderer extends DefaultActionRenderer {
        @SuppressWarnings({"rawtypes", "unchecked"})
        private void performWithConfirmation(final CockpitAction action, final ActionContext context,
                final ActionListener listener) {
            performWithConfirmationCheck(action, context, listener);
        }

    }

    /** Opens the same editor dialog as a DynamicFormField reference editor. */
    private void openFieldEditor(final WidgetInstanceManager widgetInstanceManager, final DynamicFormModel form,
            final DynamicFormFieldModel field) {
        final TypeAwareSelectionContext<DynamicFormFieldModel> selection = new TypeAwareSelectionContext<>(
                DynamicFormFieldModel._TYPECODE, field, new ArrayList<>(form.getFormFields()));
        widgetInstanceManager.sendOutput(EDIT_FIELD_OUTPUT, selection);
    }

    private void openCreateFieldWizard(final DynamicFormModel form, final WidgetInstanceManager widgetInstanceManager) {
        widgetInstanceManager.sendOutput(CREATE_FIELD_OUTPUT, Map.of(
                "TYPE_CODE", DynamicFormFieldModel._TYPECODE,
                "parentObject", form,
                "parentObjectType", DynamicFormModel._TYPECODE,
                DynamicFormModel._TYPECODE, form));
    }

    private void moveDroppedField(final DynamicFormModel form, final List<DynamicFormFieldModel> renderOrder,
            final DynamicFormFieldModel target, final DropEvent event, final WidgetInstanceManager widgetInstanceManager,
            final Component section) {
        final Object source = event.getDragged().getAttribute(FIELD_ATTRIBUTE);
        if (source instanceof DynamicFormFieldModel field && renderOrder.contains(field) && !Objects.equals(field, target)) {
            moveField(form, renderOrder, field, renderOrder.indexOf(target), widgetInstanceManager, section);
        }
    }

    /**
     * Moves a field next to the field currently at {@code targetIndex} in the render order.
     * Since that render order groups fields by step, the moved field also adopts the target's
     * step - dragging a field across a step boundary moves it into that step.
     */
    private void moveField(final DynamicFormModel form, final List<DynamicFormFieldModel> renderOrder, final DynamicFormFieldModel field,
            final int targetIndex, final WidgetInstanceManager widgetInstanceManager, final Component section) {
        final List<DynamicFormFieldModel> reordered = new ArrayList<>(form.getFormFields());
        final int sourceIndex = renderOrder.indexOf(field);
        final DynamicFormFieldModel target = renderOrder.get(targetIndex);
        reordered.remove(field);
        int insertionIndex = reordered.indexOf(target);
        if (targetIndex > sourceIndex) {
            insertionIndex++;
        }
        reordered.add(insertionIndex, field);
        form.setFormFields(reordered);
        if (!Objects.equals(field.getStep(), target.getStep())) {
            field.setStep(target.getStep());
            modelService.save(field);
        }
        modelService.save(form);
        renderPreview(section, form, widgetInstanceManager);
    }

    private Component renderInput(final DynamicFormFieldModel field) {
        final DynamicFormFieldType type = field.getFieldType();
        if (type == DynamicFormFieldType.TEXTAREA) {
            final Textbox input = textInput(field, "text");
            input.setMultiline(true);
            input.setRows(4);
            return input;
        }
        if (type == DynamicFormFieldType.NUMBER) {
            final Doublebox input = new Doublebox();
            input.setDisabled(isInactive(field));
            input.setWidth("100%");
            if (field.getDefaultValue() != null) {
                try { input.setValue(Double.valueOf(field.getDefaultValue())); } catch (final NumberFormatException ignored) { /* preview only */ }
            }
            return input;
        }
        if (type == DynamicFormFieldType.SELECT) {
            return new Html(selectMarkup(field));
        }
        if (type == DynamicFormFieldType.FILE) {
            return nativeInput("file", field);
        }
        if (type == DynamicFormFieldType.CHECKBOX) {
            final Checkbox input = new Checkbox(label("cxdevforms.preview.boolean"));
            input.setDisabled(isInactive(field));
            input.setChecked(Boolean.parseBoolean(field.getDefaultValue()));
            return input;
        }
        if (type == DynamicFormFieldType.CHECKBOXES || type == DynamicFormFieldType.RADIO) {
            final Vlayout choices = new Vlayout();
            final Radiogroup radioGroup = type == DynamicFormFieldType.RADIO ? new Radiogroup() : null;
            for (final DynamicFormFieldValueModel value : field.getFormFieldValues()) {
                if (radioGroup != null) {
                    final Radio choice = new Radio(displayName(value));
                    choice.setDisabled(isInactive(field));
                    choice.setChecked(isSelected(field, value));
                    radioGroup.appendChild(choice);
                } else {
                    final Checkbox choice = new Checkbox(displayName(value));
                    choice.setDisabled(isInactive(field));
                    choice.setChecked(isSelected(field, value));
                    choices.appendChild(choice);
                }
            }
            return radioGroup != null ? radioGroup : choices;
        }
        if (type == DynamicFormFieldType.PASSWORD || type == DynamicFormFieldType.HIDDEN) {
            return textInput(field, "password");
        }
        if (type == DynamicFormFieldType.EMAIL) {
            return textInput(field, "email");
        }
        if (type == DynamicFormFieldType.DATE) {
            return dateInput(field, false);
        }
        if (type == DynamicFormFieldType.WEEK) {
            return dateInput(field, true);
        }
        if (type == DynamicFormFieldType.COLOR) {
            return nativeInput("color", field);
        }
        return textInput(field, "text");
    }

    private Datebox dateInput(final DynamicFormFieldModel field, final boolean weekOfYear) {
        final Datebox input = new Datebox();
        input.setWeekOfYear(weekOfYear);
        input.setDisabled(isInactive(field));
        input.setWidth("100%");
        return input;
    }

    private HtmlNativeComponent nativeInput(final String type, final DynamicFormFieldModel field) {
        final HtmlNativeComponent input = new HtmlNativeComponent("input");
        input.setDynamicProperty("type", type);
        input.setDynamicProperty("value", field.getDefaultValue() == null ? "" : field.getDefaultValue());
        if (isInactive(field)) {
            input.setDynamicProperty("disabled", "disabled");
        }
        input.setDynamicProperty("style", "width: 100%;");
        return input;
    }

    private String selectMarkup(final DynamicFormFieldModel field) {
        final String disabled = isInactive(field) ? " disabled=\"disabled\"" : "";
        final StringBuilder markup = new StringBuilder("<select style=\"width: 100%;\"").append(disabled).append(">");
        markup.append("<option value=\"\">").append(escapeHtml(label("cxdevforms.preview.select"))).append("</option>");
        for (final DynamicFormFieldValueModel value : field.getFormFieldValues()) {
            markup.append("<option value=\"").append(escapeHtml(value.getId())).append("\"");
            if (Objects.equals(field.getDefaultValue(), value.getId())) {
                markup.append(" selected=\"selected\"");
            }
            markup.append(">").append(escapeHtml(displayName(value))).append("</option>");
        }
        return markup.append("</select>").toString();
    }

    private String escapeHtml(final String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private Textbox textInput(final DynamicFormFieldModel field, final String type) {
        final Textbox input = new Textbox();
        input.setType(type);
        input.setPlaceholder(field.getPlaceholder());
        input.setValue(field.getDefaultValue());
        input.setDisabled(isInactive(field));
        input.setWidth("100%");
        return input;
    }

    private String validationText(final DynamicFormFieldModel field) {
        final DynamicFormFieldType type = field.getFieldType();
        final List<String> annotations = new ArrayList<>();
        if (type == DynamicFormFieldType.TEXT || type == DynamicFormFieldType.TEXTAREA) {
            final String length = lengthText(field.getMinLength(), field.getMaxLength());
            if (length != null) {
                annotations.add(length);
            }
        }
        if (type == DynamicFormFieldType.NUMBER) {
            final String range = numberRangeText(field.getMinValue(), field.getMaxValue());
            if (range != null) {
                annotations.add(range);
            }
            if (!isBlank(field.getDefaultValue())) {
                annotations.add(label("cxdevforms.preview.defaultValue", field.getDefaultValue()));
            }
        }
        if (type == DynamicFormFieldType.FILE && field.getMaxLength() != null) {
            return label("cxdevforms.preview.fileSize", field.getMaxLength());
        }
        if (type == DynamicFormFieldType.SELECT || type == DynamicFormFieldType.CHECKBOXES || type == DynamicFormFieldType.RADIO) {
            final String options = optionLabels(field.getFormFieldValues());
            if (!options.isBlank()) {
                annotations.add(label("cxdevforms.preview.validValues", options));
            }
        }
        return annotations.isEmpty() ? null : String.join(" · ", annotations);
    }

    private String lengthText(final Integer minimum, final Integer maximum) {
        if (minimum != null && maximum != null) {
            return label("cxdevforms.preview.length.range", minimum, maximum);
        }
        if (minimum != null) {
            return label("cxdevforms.preview.length.minimum", minimum);
        }
        if (maximum != null) {
            return label("cxdevforms.preview.length.maximum", maximum);
        }
        return null;
    }

    private String numberRangeText(final Double minimum, final Double maximum) {
        if (minimum != null && maximum != null) {
            return label("cxdevforms.preview.number.range", formatNumber(minimum), formatNumber(maximum));
        }
        if (minimum != null) {
            return label("cxdevforms.preview.number.minimum", formatNumber(minimum));
        }
        if (maximum != null) {
            return label("cxdevforms.preview.number.maximum", formatNumber(maximum));
        }
        return null;
    }

    private String formatNumber(final Double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private boolean isInactive(final DynamicFormFieldModel field) {
        return Boolean.FALSE.equals(field.isActive());
    }

    /**
     * A preview has no persisted submission state. The configured default value
     * represents its initial selected state and therefore controls conditional fields.
     */
    private boolean isSelected(final DynamicFormFieldModel field, final DynamicFormFieldValueModel value) {
        if (field.getDefaultValue() == null || value.getId() == null) {
            return false;
        }
        return List.of(field.getDefaultValue().split(",")).stream()
                .map(String::trim)
                .anyMatch(value.getId()::equals);
    }

    private void appendRule(final Component parent, final String rule) {
        if (rule != null) {
            final Label validation = new Label(rule);
            validation.setStyle("display: block; font-style: italic; color: #5b5b5b; white-space: pre-wrap; margin: 10px 0px 0px 0px;");
            parent.appendChild(validation);
        }
    }

    private String visibilityText(final DynamicFormFieldModel field) {
        final DynamicFormFieldValueModel parentValue = field.getParentFieldValue();
        if (parentValue == null) {
            return null;
        }
        final DynamicFormFieldModel parentField = parentValue.getField();
        return label("cxdevforms.preview.visibleIf", displayName(parentField), displayName(parentValue));
    }

    private List<DynamicFormFieldModel> sorted(final Collection<DynamicFormFieldModel> fields) {
        return fields.stream().sorted(Comparator.comparing(DynamicFormFieldModel::getId, Comparator.nullsLast(String::compareTo))).toList();
    }

    private String optionLabels(final Collection<DynamicFormFieldValueModel> values) {
        return values == null ? "" : values.stream().map(this::displayName).collect(joining(", "));
    }

    private String displayName(final DynamicFormFieldModel field) {
        return field.getLabel() == null || field.getLabel().isBlank() ? field.getId() : field.getLabel();
    }

    private String displayName(final DynamicFormFieldValueModel value) {
        return value.getLabel() == null || value.getLabel().isBlank() ? value.getId() : value.getLabel();
    }

    private String displayName(final DynamicFormStepModel step) {
        return step.getLabel() == null || step.getLabel().isBlank() ? step.getId() : step.getLabel();
    }

    private String label(final String key, final Object... arguments) {
        return Labels.getLabel(key, arguments);
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
