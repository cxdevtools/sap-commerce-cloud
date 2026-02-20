package tools.sapcx.commerce.reporting.search;

import de.hybris.platform.impex.enums.ImpExValidationModeEnum;
import de.hybris.platform.servicelayer.impex.*;
import de.hybris.platform.servicelayer.impex.impl.StreamBasedImpExResource;
import de.hybris.platform.servicelayer.media.MediaService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.CharUtils.toChar;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

public class ImpExExportGenericSearchService implements GenericSearchService {
    private final ExportService exportService;
    private final MediaService mediaService;

    public ImpExExportGenericSearchService(ExportService exportService, MediaService mediaService) {
        this.exportService = exportService;
        this.mediaService = mediaService;
    }

    @Override
    public GenericSearchResult search(String query, Map<String, Object> parameters, Map<String, Object> configuration) {
        final ImpExValidationResult validationResult = exportService.validateExportScript(query, ImpExValidationModeEnum.EXPORT_ONLY);
        if (!validationResult.isSuccessful()) {
            return new GenericSearchResult(validationResult.getFailureCause());
        }

        try {
            final ExportConfig exportConfig = prepareExportConfiguration(configuration);
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(query.getBytes(exportConfig.getEncoding()));
            exportConfig.setScript(new StreamBasedImpExResource(inputStream, exportConfig.getEncoding()));

            final ExportResult exportResult = exportService.exportData(exportConfig);
            if (exportResult.isError()) {
                return new GenericSearchResult("Export failed!");
            }

            final String mime = exportResult.getExportedData().getMime();
            final InputStream exportedDataStream = mediaService.getStreamFromMedia(exportResult.getExportedData());

            // TODO replace empty result with result set from export
            final List<GenericSearchResultHeader> headers = List.of();
            final List<Map<GenericSearchResultHeader, String>> values = List.of();
            return new GenericSearchResult(headers, values);
        } catch (UnsupportedEncodingException e) {
            return new GenericSearchResult("Unsupported encoding exception: " + e.getMessage());
        }
    }

    private ExportConfig prepareExportConfiguration(Map<String, Object> configuration) {
        final ExportConfig exportConfig = new ExportConfig();
        exportConfig.setSingleFile(true);
        exportConfig.setSynchronous(true);
        exportConfig.setFailOnError(true);
        exportConfig.setValidationMode(ExportConfig.ValidationMode.RELAXED);

        exportConfig.setEncoding(defaultIfBlank((String) configuration.get("csvEncoding"), "UTF-8"));
        exportConfig.setCommentCharacter(toChar((Character) configuration.get("csvCommentChar"), exportConfig.getCommentCharacter()));
        exportConfig.setFieldSeparator(toChar((Character) configuration.get("csvFieldSeparator"), exportConfig.getFieldSeparator()));
        exportConfig.setQuoteCharacter(toChar((Character) configuration.get("csvTextSeparator"), exportConfig.getQuoteCharacter()));
        return exportConfig;
    }
}
