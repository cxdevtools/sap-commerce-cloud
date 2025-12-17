package tools.sapcx.commerce.reporting.generator;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.media.services.MimeService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.mail2.core.EmailException;
import org.apache.commons.mail2.jakarta.HtmlEmail;
import org.assertj.core.util.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.sapcx.commerce.reporting.model.QueryReportConfigurationModel;
import tools.sapcx.commerce.reporting.model.ReportGenerationScheduleModel;
import tools.sapcx.commerce.reporting.report.ReportService;
import tools.sapcx.commerce.reporting.report.data.QueryFileConfigurationData;
import tools.sapcx.commerce.reporting.search.GenericSearchResult;
import tools.sapcx.commerce.reporting.search.GenericSearchService;
import tools.sapcx.commerce.toolkit.email.HtmlEmailGenerator;
import tools.sapcx.commerce.toolkit.email.HtmlEmailService;

import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;

public class ReportGeneratorJobPerformable extends AbstractJobPerformable<ReportGenerationScheduleModel> {
	private static final Logger LOG = LoggerFactory.getLogger(ReportGeneratorJobPerformable.class);
	private static final int BYTES_TO_READ = 20;

	private final GenericSearchService genericSearchService;
	private final ReportService reportService;
	private final HtmlEmailGenerator htmlEmailGenerator;
	private final HtmlEmailService htmlEmailService;
	private final MimeService mimeService;
	private final Converter<QueryReportConfigurationModel, QueryFileConfigurationData> queryConfigurationConverter;

	public ReportGeneratorJobPerformable(
			GenericSearchService genericSearchService,
			ReportService reportService,
			HtmlEmailGenerator htmlEmailGenerator,
			HtmlEmailService htmlEmailService,
			MimeService mimeService,
			Converter<QueryReportConfigurationModel, QueryFileConfigurationData> queryConfigurationConverter) {
		this.genericSearchService = genericSearchService;
		this.reportService = reportService;
		this.htmlEmailGenerator = htmlEmailGenerator;
		this.htmlEmailService = htmlEmailService;
		this.mimeService = mimeService;
		this.queryConfigurationConverter = queryConfigurationConverter;
	}

	@Override
	public PerformResult perform(ReportGenerationScheduleModel schedule) {
		String code = schedule.getCode();
		LOG.info(String.format("Report generation started with job schedule: %s", code));

		Collection<QueryReportConfigurationModel> reports = schedule.getReports();
		if (reports == null) {
			LOG.info("No reports found for job schedule, skipping.");
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		}

		List<QueryReportConfigurationModel> reportsWithErrors = new ArrayList<>(reports.size());
		for (QueryReportConfigurationModel report : reports) {
			if (!generateAndSendReport(report)) {
				reportsWithErrors.add(report);
			}
		}

		if (reportsWithErrors.isEmpty()) {
			return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
		} else {
			StringBuilder logMessage = new StringBuilder()
					.append("Report generation finished with errors on job schedule: ")
					.append(code)
					.append("\n")
					.append("The following reports could not be generated or sent:")
					.append("\n");
			reportsWithErrors.forEach(report -> logMessage
					.append("\t")
					.append("Report: '")
					.append(report.getTitle())
					.append("', Format: '")
					.append(report.getExportFormat())
					.append("'\n"));
			LOG.warn(logMessage.toString());
			return new PerformResult(CronJobResult.ERROR, CronJobStatus.FINISHED);
		}
	}

	private boolean generateAndSendReport(QueryReportConfigurationModel report) {
		String query = report.getSearchQuery();
		Map<String, Object> params = reportService.getReportParameters(report);
		LOG.debug(String.format("Executing query '%s' for report '%s'", query, report.getTitle()));

		Optional<File> reportFile = Optional.empty();
		Optional<File> zipFile = Optional.empty();
		try {
			GenericSearchResult searchResult = genericSearchService.search(query, params);
			if (searchResult.hasError()) {
				LOG.warn(String.format("Error executing query '%s' for report '%s'", query, report.getTitle()));
				return false;
			} else if (searchResult.getResultRows() == 0 && !report.getEmailEmptyResult()) {
				LOG.info(String.format("No results found for report '%s', skip sending.", report.getTitle()));
				return true;
			}

			reportFile = reportService.getReportFile(queryConfigurationConverter.convert(report), searchResult);

			HtmlEmail mail = createResultEmail(report, searchResult, reportFile.isPresent());
			if (reportFile.isPresent()) {
				boolean useCompression = BooleanUtils.isTrue(report.getCompress());
				addEmailAttachment(mail, reportFile.get(), useCompression, report.getTitle());
			}
			htmlEmailService.sendEmail(mail);
			return true;
		} catch (EmailException e) {
			LOG.error(String.format("Error creating email for report '%s'", report.getTitle()), e);
			return false;
		} finally {
			reportFile.ifPresent(File::delete);
			zipFile.ifPresent(File::delete);
		}
	}

	private HtmlEmail createResultEmail(QueryReportConfigurationModel report, GenericSearchResult search,
			boolean hasAttachments) throws EmailException {
		String title = report.getTitle();
		String description = getDescription(search, hasAttachments, report.getDescription());
		return htmlEmailGenerator.newHtmlEmail()
				.subject(title)
				.body(description)
				.custom(builder -> emptyIfNull(report.getEmailRecipients()).forEach(builder::to))
				.build();
	}

	private String getDescription(GenericSearchResult search, boolean hasAttachment, String description) {
		if (search.hasError()) {
			return search.getError();
		} else if (!hasAttachment) {
			return "Error generating report";
		} else {
			return StringUtils.defaultString(description);
		}
	}

	protected void addEmailAttachment(HtmlEmail mail, File reportFile, boolean useCompression, String title) throws EmailException {
		File file = reportFile;
		String dateTime = DateFormatUtils.format(new Date(), "yyyyMMdd-HHmmss");
		String extension = FilenameUtils.getExtension(reportFile.getAbsolutePath());
		String filename = String.format("%s %s.%s", title, dateTime, extension);

		if (useCompression) {
			try {
				File zipFile = createZipArchive(reportFile);
				file = zipFile;
				filename = filename + ".zip";
			} catch (IOException e) {
				LOG.warn(String.format("Could not create ZIP archive. Sending report %s without compressing.", title), e);
			}
		}

		try {
			mail.attach(getFileDataSource(file), filename, StringUtils.EMPTY);
		} catch (IOException e) {
			throw new EmailException(String.format("Could not attach report to mail. Sending report %s without attachments.", title), e);
		}
	}

	@VisibleForTesting
	File createZipArchive(File reportFile) throws IOException {
		Path filePath = reportFile.toPath();
		String fileName = filePath.getFileName().toString();
		String fileNameForZip = fileName + ".zip";
		Path zipFilePath = filePath.getParent().resolve(fileNameForZip);

		Files.createFile(zipFilePath);
		File zipFile = zipFilePath.toFile();

		try (ZipOutputStream zos = new ZipOutputStream(FileUtils.openOutputStream(zipFile))) {
			ZipEntry zipEntry = new ZipEntry(fileName);
			zos.putNextEntry(zipEntry);
			FileUtils.copyFile(reportFile, zos);
		}

		return zipFile;
	}

	protected DataSource getFileDataSource(File file) throws IOException {
		String mime = getMime(file);
		InputStream is = new FileInputStream(file);
		return new ByteArrayDataSource(is, mime);
	}

	private String getMime(File file) {
		try (InputStream dataStream = new FileInputStream(file)) {
			int availableBytes = dataStream.available();
			int bytesToRead = availableBytes < BYTES_TO_READ ? availableBytes : BYTES_TO_READ;
			if (bytesToRead > 0) {
				byte[] firstBytes = new byte[bytesToRead];
				dataStream.read(firstBytes, 0, bytesToRead);
				return this.mimeService.getBestMime(file.getName(), firstBytes, "application/octet-stream");
			}
		} catch (FileNotFoundException e) {
			LOG.warn(String.format("Mime type detection failed, reason: Cannot find file %s", file.getAbsolutePath()), e);
		} catch (IOException e) {
			LOG.warn(String.format("Mime type detection failed, reason: Cannot read file %s", file.getAbsolutePath()), e);
		}
		return "application/octet-stream";
	}
}
