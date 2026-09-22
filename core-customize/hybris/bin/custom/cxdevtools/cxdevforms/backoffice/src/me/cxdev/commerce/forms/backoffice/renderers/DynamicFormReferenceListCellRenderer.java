package me.cxdev.commerce.forms.backoffice.renderers;

import com.hybris.cockpitng.core.config.impl.jaxb.listview.ListColumn;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.collectionbrowser.mold.impl.listview.renderer.DefaultListCellRenderer;
import com.hybris.cockpitng.widgets.util.QualifierLabel;

import org.zkoss.zul.Listcell;

import me.cxdev.commerce.forms.model.DynamicFormFieldModel;
import me.cxdev.commerce.forms.model.DynamicFormFieldValueModel;
import me.cxdev.commerce.forms.model.DynamicFormModel;

/** Renders form and field reference details as separate Backoffice list columns. */
public class DynamicFormReferenceListCellRenderer extends DefaultListCellRenderer {

    private static final String FORM_TITLE = "formTitle";
    private static final String FIELD_FORM_TITLE = "fieldFormTitle";
    private static final String FIELD_LABEL = "fieldLabel";

    @Override
    protected QualifierLabel getLabelText(
            final Listcell parent,
            final ListColumn column,
            final Object item,
            final DataType dataType,
            final WidgetInstanceManager widgetInstanceManager) {
        if (item instanceof DynamicFormFieldModel field && FORM_TITLE.equals(column.getQualifier())) {
            return new QualifierLabel(formTitle(field.getForm()));
        }
        if (item instanceof DynamicFormFieldValueModel value) {
            final DynamicFormFieldModel field = value.getField();
            if (FIELD_FORM_TITLE.equals(column.getQualifier())) {
                return new QualifierLabel(formTitle(field == null ? null : field.getForm()));
            }
            if (FIELD_LABEL.equals(column.getQualifier())) {
                return new QualifierLabel(label(field == null ? null : field.getLabel(), field == null ? null : field.getId()));
            }
        }
        return super.getLabelText(parent, column, item, dataType, widgetInstanceManager);
    }

    private String formTitle(final DynamicFormModel form) {
        return label(form == null ? null : form.getTitle(), form == null ? null : form.getId());
    }

    private String label(final String value, final String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
