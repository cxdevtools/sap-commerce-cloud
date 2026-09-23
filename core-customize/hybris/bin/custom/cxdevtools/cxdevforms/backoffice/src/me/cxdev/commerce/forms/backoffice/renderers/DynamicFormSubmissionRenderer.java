package me.cxdev.commerce.forms.backoffice.renderers;

import com.hybris.cockpitng.core.config.impl.jaxb.editorarea.AbstractSection;
import com.hybris.cockpitng.dataaccess.facades.type.DataType;
import com.hybris.cockpitng.engine.WidgetInstanceManager;
import com.hybris.cockpitng.widgets.common.WidgetComponentRenderer;

import org.zkoss.util.Locales;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Caption;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;

import me.cxdev.commerce.forms.model.DynamicFormSubmissionModel;
import me.cxdev.commerce.forms.submission.SubmissionPresentationService;

/** Read-only, escaped labels and values grouped by configured form step. @since 5.0.2 */
public class DynamicFormSubmissionRenderer implements WidgetComponentRenderer<Component, AbstractSection, Object> {
	private final SubmissionPresentationService presentationService;
	public DynamicFormSubmissionRenderer(final SubmissionPresentationService presentationService) {
		this.presentationService = presentationService;
	}

	@Override
	public void render(final Component parent, final AbstractSection configuration, final Object object,
			final DataType dataType, final WidgetInstanceManager widgetInstanceManager) {
		if (!(object instanceof DynamicFormSubmissionModel submission)) {
			return;
		}
		try {
			final var view = presentationService.present(submission, Locales.getCurrent());
			if (view.sections().isEmpty()) {
				parent.appendChild(new Label(Labels.getLabel("cxdevforms.submission.empty")));
			}
			for (final var section : view.sections()) {
				final Groupbox group = new Groupbox();
				group.setClosable(false);
				group.appendChild(new Caption(section.title()));
				for (final var answer : section.answers()) {
					final Div row = new Div();
					final Div heading = new Div();
					heading.appendChild(new Label(answer.label()));
					row.appendChild(heading);
					final Label value = new Label(answer.value());
					value.setPre(true);
					value.setStyle("white-space: pre-wrap; overflow-wrap: anywhere;");
					row.appendChild(value);
					group.appendChild(row);
				}
				parent.appendChild(group);
			}
		} catch (final RuntimeException e) {
			parent.appendChild(new Label(Labels.getLabel("cxdevforms.submission.unreadable")));
		}
	}
}
