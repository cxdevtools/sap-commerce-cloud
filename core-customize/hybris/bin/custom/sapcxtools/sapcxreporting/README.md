# SAP CX Reporting

The `sapcxreporting` extension provides reporting capabilities within the Backoffice. With support of flexible search queries a report
configuration with dynamic configuration properties, i.e. one can use named properties within the query and link them within the backoffice
application to existing objects like products, categories, catalog versions, user or groups, and configuration values from the properties.

## FEATURE DESCRIPTION

- Provide an easy way to extract reports from the system database in various export formats, e.g. CSV (UTF-8), EXCEL
- Use `FlexibleSearchService` to provide a generic way to specify the SQL statements without having to care about the database layer
- Refers to real item instances in a configurable way within the backoffice application, no need to use PKs
- Schedules for execution of reports in frequently manner, e.g. for regular data quality reports once a week or month
- Extensibility for custom configuration parameters or other export formats

### How to activate and use

The extension itself introduces a ServicelayerJob that will be created during system initialization / update. Still, it will not be
executed within any further configuration.

One needs to create a `QueryReportConfigurationModel` instance with all required attributes to activate any of the functionality. The
report generation can be triggered manually by using the backoffice actions `validate` and `download` on the
`QueryReportConfigurationModel` instance, or by setting up a `ReportGenerationScheduleModel` that extends the `CronJobModel` and performs
one or multiple reports.

When triggered manually via the backoffice action, the reports will be downloaded directly and the "compress" setting will be ignored.
The compression is primary useful for sending email to avoid the email size exceeds limit problem within many companies.  

The configuration of a report should be self-explaining:
- `title`, a title for the report (also used as filename prefix)
- `description`, describes the report (also used as email body)
- `exportFormat`, the file format for the export
- `emailRecipients`, list of email addresses the report is sent to (only when executed automatically from a schedule)
- `compress`, use ZIP compression for the report (only when sent via email)
- `search` query, the flexible search query to execute against the database
- `parameters`, the named parameters to inject to the flexible search service
- `encoding`, the file encoding, default=UTF-8 (only via CSV)
- `commentchar`, the comment character, default=# (only via CSV)
- `fieldseparator`, the field separator character, default=; (only via CSV)
- `textseparator`, the quote character, default=" (only via CSV)
- `linebreak`, the line break, default=\n (only via CSV)
- `highlightHeader`, formats the first line of the result with bold (only with EXCEL)
- `alternatinglines`, formats the value lines of the result in alternating colors (only with EXCEL)
- `freezeHeader`, freezes the first line of the result (only with EXCEL)
- `activateFilter`, adds filters to the first line of the result (only with EXCEL)
- `autosizeColumns`, automatically resizes the column width (only with EXCEL)
          
You can provide your own Subclasses of `QueryReportConfigurationParameter` to support your customer project with additional lookup
capabilities on custom objects, e.g. special item types introduces by the project team. To get an example, have a look into the
`sapcxreporting-items.xml` file to see how it was done for some standard types.

### Known limitations of the implementation:
- with large result sets the POI library reaches its limitations, use CSV as a fallback

### Configuration parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| sapcxreporting.report.format.csv.encoding | character | default option for new reports, default: `UTF-8` |
| sapcxreporting.report.format.csv.commentchar | character | default option for new reports, default: `#` |
| sapcxreporting.report.format.csv.fieldseparator | character | default option for new reports, default: `;` |
| sapcxreporting.report.format.csv.textseparator | character | default option for new reports, default: `"` |
| sapcxreporting.report.format.csv.linebreak | character | default option for new reports, default: `\n` |
| sapcxreporting.report.format.excel.highlightheader | boolean | default option for new reports, default: `false` |
| sapcxreporting.report.format.excel.alternatinglines | boolean | default option for new reports, default: `false` |
| sapcxreporting.report.format.excel.freezeheader | boolean | default option for new reports, default: `true` |
| sapcxreporting.report.format.excel.activatefilter | boolean | default option for new reports, default: `true` |
| sapcxreporting.report.format.excel.autosizecolumns | boolean | default option for new reports, default: `true` |



## License

_Licensed under the Apache License, Version 2.0, January 2004_

_Copyright 2026, SAP CX Tools_