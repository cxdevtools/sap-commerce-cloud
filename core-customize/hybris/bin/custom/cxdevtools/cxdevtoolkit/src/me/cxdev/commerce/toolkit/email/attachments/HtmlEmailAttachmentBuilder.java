package me.cxdev.commerce.toolkit.email.attachments;

import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;

public interface HtmlEmailAttachmentBuilder {
	HtmlEmailAttachmentBuilder name(String name);

	HtmlEmailAttachmentBuilder description(String description);

	void attach(HtmlEmail email) throws EmailException;
}
