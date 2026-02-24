package me.cxdev.commerce.toolkit.email.attachments;

import de.hybris.platform.core.Registry;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.media.NoDataAvailableException;

import org.apache.commons.mail2.core.EmailException;

import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;

public class MediaBasedHtmlEmailAttachmentBuilder extends AbstractHtmlEmailAttachmentBuilder {
	private MediaModel media;

	public MediaBasedHtmlEmailAttachmentBuilder(MediaModel media) {
		this.media = media;
		this.name(media.getRealFileName());
		this.description(media.getDescription());
	}

	@Override
	protected DataSource getDataSource() throws EmailException {
		try {
			MediaService mediaService = Registry.getApplicationContext().getBean(MediaService.class);
			return new ByteArrayDataSource(mediaService.getDataFromMedia(media), media.getMime());
		} catch (NoDataAvailableException e) {
			throw new EmailException("Cannot attach empty file from media: " + media.getCode(), e);
		}
	}
}
