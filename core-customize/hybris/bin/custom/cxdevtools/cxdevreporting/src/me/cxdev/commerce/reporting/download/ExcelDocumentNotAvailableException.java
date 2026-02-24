package me.cxdev.commerce.reporting.download;

public class ExcelDocumentNotAvailableException extends RuntimeException {
	public ExcelDocumentNotAvailableException() {
	}

	public ExcelDocumentNotAvailableException(String message) {
		super(message);
	}

	public ExcelDocumentNotAvailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExcelDocumentNotAvailableException(Throwable cause) {
		super(cause);
	}

	public ExcelDocumentNotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
