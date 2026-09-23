package me.cxdev.commerce.forms.event;

import de.hybris.platform.core.PK;
import de.hybris.platform.servicelayer.event.ClusterAwareEvent;
import de.hybris.platform.servicelayer.event.TransactionAwareEvent;
import de.hybris.platform.servicelayer.event.events.AbstractEvent;

/** Broadcast after commit; listeners load the submission by PK. @since 5.0.2 */
public class DynamicFormSubmissionCreatedEvent extends AbstractEvent implements ClusterAwareEvent, TransactionAwareEvent {
	private static final long serialVersionUID = 1L;
	private final PK submissionPk;
	private final String submissionId;
	private final String formId;
	private final int originNodeId;
	public DynamicFormSubmissionCreatedEvent(final PK submissionPk, final String submissionId, final String formId, final int originNodeId) {
		this.submissionPk = submissionPk;
		this.submissionId = submissionId;
		this.formId = formId;
		this.originNodeId = originNodeId;
	}

	public PK getSubmissionPk() {
		return submissionPk;
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public String getFormId() {
		return formId;
	}

	public int getOriginNodeId() {
		return originNodeId;
	}

	@Override
	public boolean publish(final int sourceNodeId, final int targetNodeId) {
		return true;
	}

	@Override
	public boolean publishOnCommitOnly() {
		return true;
	}

	@Override
	public Object getId() {
		return getClass().getName() + ":" + submissionId;
	}
}
