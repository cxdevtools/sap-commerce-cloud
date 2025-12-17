package tools.sapcx.commerce.reporting.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

class SelfDeletingFileInputStream extends FileInputStream {
	private final File file;

	public SelfDeletingFileInputStream(File file) throws FileNotFoundException {
		super(file);
		this.file = file;
	}

	@Override
	public void close() throws IOException {
		super.close();
		FileUtils.deleteQuietly(file);
	}
}
