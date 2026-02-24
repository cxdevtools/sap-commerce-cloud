package me.cxdev.commerce.toolkit.email.attachments;

import java.io.File;
import java.net.URL;

import de.hybris.platform.core.model.media.MediaModel;

public final class HtmlEmailAttachmentBuilders {
	public static HtmlEmailAttachmentBuilder forFile(File file) {
		return new FileBasedHtmlEmailAttachmentBuilder(file);
	}

	public static HtmlEmailAttachmentBuilder forUrl(URL url) {
		return new URLBasedHtmlEmailAttachmentBuilder(url);
	}

	public static HtmlEmailAttachmentBuilder forMedia(MediaModel media) {
		return new MediaBasedHtmlEmailAttachmentBuilder(media);
	}

	private HtmlEmailAttachmentBuilders() {
	}
}
