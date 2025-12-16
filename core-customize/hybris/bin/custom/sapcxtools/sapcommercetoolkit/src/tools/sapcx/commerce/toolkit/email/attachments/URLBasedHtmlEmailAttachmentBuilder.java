package tools.sapcx.commerce.toolkit.email.attachments;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.mail2.core.EmailException;

import jakarta.activation.DataSource;
import jakarta.activation.URLDataSource;

public class URLBasedHtmlEmailAttachmentBuilder extends AbstractHtmlEmailAttachmentBuilder {
	private URL url;

	public URLBasedHtmlEmailAttachmentBuilder(URL url) {
		this.url = url;
	}

	@Override
	protected DataSource getDataSource() throws EmailException {
		try (InputStream is = url.openStream()) {
			return new URLDataSource(url);
		} catch (IOException e) {
			throw new EmailException("Invalid URL set: " + url, e);
		}
	}
}
