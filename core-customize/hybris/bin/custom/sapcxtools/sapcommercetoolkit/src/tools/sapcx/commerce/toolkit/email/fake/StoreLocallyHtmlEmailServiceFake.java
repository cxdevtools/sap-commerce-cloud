package tools.sapcx.commerce.toolkit.email.fake;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import de.hybris.platform.core.model.media.MediaFolderModel;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.servicelayer.media.MediaService;
import de.hybris.platform.servicelayer.model.ModelService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.sapcx.commerce.toolkit.email.HtmlEmailService;
import tools.sapcx.commerce.toolkit.model.LocallyStoredEmailModel;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class StoreLocallyHtmlEmailServiceFake implements HtmlEmailService {
	private static final Logger LOG = LoggerFactory.getLogger(StoreLocallyHtmlEmailServiceFake.class);
	private static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmmssS";
	private static final String MEDIACODE_FORMAT = "fake-email_%s";
	private static final String DEFAULT_FILENAME_PATTERN = "{timestamp}_{subject}.{extension}";
	private static final String DEFAULT_EXTENSION = "eml";
	private static final String METHOD_FILE = "file";
	private static final String METHOD_DATABASE = "database";

	private ModelService modelService;
	private MediaService mediaService;
	private boolean useFilesystem;
	private boolean useDatabase;
	private String directory;
	private String filenamePattern;
	private String extension;
	private String mediaFolder;

	public static StoreLocallyHtmlEmailServiceFake storeInFilesystem(String directory) {
		return storeInFilesystem(directory, DEFAULT_FILENAME_PATTERN, DEFAULT_EXTENSION);
	}

	public static StoreLocallyHtmlEmailServiceFake storeInFilesystem(
			String directory, String filenamePattern, String extension) {
		return new StoreLocallyHtmlEmailServiceFake(null, null, METHOD_FILE, directory, filenamePattern, extension, null);
	}

	public static StoreLocallyHtmlEmailServiceFake storeInDatabase(
			String mediaFolder, ModelService modelService, MediaService mediaService) {
		return new StoreLocallyHtmlEmailServiceFake(modelService, mediaService, METHOD_DATABASE, null, DEFAULT_FILENAME_PATTERN, DEFAULT_EXTENSION, mediaFolder);
	}

	public static StoreLocallyHtmlEmailServiceFake storeInDatabase(
			String mediaFolder, String filenamePattern, String extension,
			ModelService modelService, MediaService mediaService) {
		return new StoreLocallyHtmlEmailServiceFake(modelService, mediaService, METHOD_DATABASE, null, filenamePattern, extension, mediaFolder);
	}

	public StoreLocallyHtmlEmailServiceFake(
			ModelService modelService, MediaService mediaService, String method,
			String directory, String filenamePattern, String extension, String mediaFolder) {
		this.modelService = modelService;
		this.mediaService = mediaService;
		configurePersistenceMethod(method);
		this.directory = directory;
		this.filenamePattern = filenamePattern;
		this.extension = extension;
		this.mediaFolder = mediaFolder;
	}

	@Override
	public String sendEmailInternal(HtmlEmail email) throws EmailException {
		// first let the email generate the mime message, which also sets sent date
		buildHtmlEmail(email);

		storeToFile(email);
		storeToDatabase(email);

		MimeMessage mimeMessage = email.getMimeMessage();
		try {
			return mimeMessage.getMessageID();
		} catch (MessagingException e) {
			LOG.error("Message ID for email could not be retrieved: " + e.getLocalizedMessage(), e);
			return null;
		}
	}

	private void storeToFile(HtmlEmail email) {
		if (useFilesystem) {
			MimeMessage mimeMessage = email.getMimeMessage();
			File outputFile = getOutputDirectory().resolve(getFilename(email)).toFile();
			try (FileOutputStream out = new FileOutputStream(outputFile)) {
				mimeMessage.writeTo(out);
				LOG.info("New email successfully stored at: " + outputFile.getAbsolutePath());
			} catch (IOException | MessagingException e) {
				LOG.error("Email could not persisted to local storage: " + e.getLocalizedMessage(), e);
			}
		}
	}

	private void storeToDatabase(HtmlEmail email) {
		if (useDatabase) {
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				MimeMessage mimeMessage = email.getMimeMessage();
				mimeMessage.writeTo(out);

				LocallyStoredEmailModel emailMedia = modelService.create(LocallyStoredEmailModel.class);
				emailMedia.setCode(getMediaCode(email, mimeMessage));
				emailMedia.setRealFileName(getFilename(email));
				emailMedia.setSender(emailArrayToString(mimeMessage.getFrom()));
				emailMedia.setRecipients(emailArrayToString(mimeMessage.getRecipients(Message.RecipientType.TO)));
				emailMedia.setDescription(mimeMessage.getSubject());
				emailMedia.setMime("application/octet-stream");
				emailMedia.setFolder(getOrCreateMediaFolder());
				modelService.save(emailMedia);

				mediaService.setDataForMedia(emailMedia, out.toByteArray());

				LOG.info("New email successfully stored as media with PK " + emailMedia.getPk());
			} catch (IOException | MessagingException e) {
				LOG.error("Email could not persisted to local database: " + e.getLocalizedMessage(), e);
			}
		}
	}

	private MediaFolderModel getOrCreateMediaFolder() {
		try {
			return mediaService.getFolder(mediaFolder);
		} catch (UnknownIdentifierException e) {
			MediaFolderModel folder = modelService.create(MediaFolderModel.class);
			folder.setQualifier(mediaFolder);
			folder.setPath(mediaFolder);
			modelService.save(folder);
		}

		// Retry after creation
		return mediaService.getFolder(mediaFolder);
	}

	private void buildHtmlEmail(HtmlEmail email) throws EmailException {
		if (StringUtils.isBlank(email.getHostName())) {
			// fix hostname to localhost to avoid exceptions during build of mime message
			email.setHostName("localhost");
		}
		email.buildMimeMessage();
	}

	private Path getOutputDirectory() {
		Path outputDir = Paths.get(directory);
		if (outputDir.toFile().mkdirs()) {
			LOG.info("Output directory of local email storage was created at: " + outputDir.toAbsolutePath().toString());
		}
		return outputDir;
	}

	@VisibleForTesting
	String getFilename(HtmlEmail email) {
		Date sentDate = email.getSentDate() != null ? email.getSentDate() : new Date();
		Map<String, String> replacements = Map.of(
				"{timestamp}", Long.toString(sentDate.getTime()),
				"{datetime}", new SimpleDateFormat(TIMESTAMP_FORMAT).format(sentDate),
				"{subject}", email.getSubject() != null ? email.getSubject() : "",
				"{from}", escapeAddress(email.getFromAddress()),
				"{to}", escapeAddress(emptyIfNull(email.getToAddresses()).stream().findAny().orElse(null)),
				"{extension}", this.extension);

		String newFilename = filenamePattern;
		for (Map.Entry<String, String> replacement : replacements.entrySet()) {
			newFilename = newFilename.replace(replacement.getKey(), replacement.getValue());
		}
		return newFilename;
	}

	private String escapeAddress(InternetAddress address) {
		return (address != null) ? address.getAddress().toLowerCase(Locale.ROOT).replaceAll("@", "__at__") : "";
	}

	private String getMediaCode(HtmlEmail email, MimeMessage mimeMessage) throws MessagingException {
		if (mimeMessage != null && mimeMessage.getMessageID() != null) {
			return mimeMessage.getMessageID();
		} else {
			Date sentDate = email.getSentDate() != null ? email.getSentDate() : new Date();
			String timestamp = new SimpleDateFormat(TIMESTAMP_FORMAT).format(sentDate);
			return String.format(MEDIACODE_FORMAT, timestamp);
		}
	}

	private String emailArrayToString(Address[] addresses) {
		return (addresses == null) ? null
				: Arrays.stream(addresses)
						.map(Address::toString)
						.collect(joining(", "));
	}

	private void configurePersistenceMethod(String method) {
		if (METHOD_FILE.equalsIgnoreCase(method)) {
			LOG.info("Using SMTP fake service storing emails locally into the file system.");
			this.useFilesystem = true;
			this.useDatabase = false;
		} else if (METHOD_DATABASE.equalsIgnoreCase(method)) {
			LOG.info("Using SMTP fake service storing emails locally into the database.");
			this.useFilesystem = false;
			this.useDatabase = true;
		} else {
			throw new IllegalArgumentException(String.format("Persistence method must be either '%s' or '%s'!", METHOD_FILE, METHOD_DATABASE));
		}
	}
}
