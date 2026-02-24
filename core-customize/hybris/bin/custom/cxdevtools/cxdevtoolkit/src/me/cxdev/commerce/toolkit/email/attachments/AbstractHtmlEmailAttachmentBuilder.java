package me.cxdev.commerce.toolkit.email.attachments;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

import jakarta.activation.DataSource;

public abstract class AbstractHtmlEmailAttachmentBuilder implements HtmlEmailAttachmentBuilder {
	private String name;
	private String description;

	@Override
	public HtmlEmailAttachmentBuilder name(String name) {
		this.name = name;
		return this;
	}

	@Override
	public HtmlEmailAttachmentBuilder description(String description) {
		this.description = description;
		return this;
	}

	@Override
	public void attach(HtmlEmail email) throws EmailException {
		email.attach(getDataSource(), name, description);
	}

	protected abstract DataSource getDataSource() throws EmailException;
}
