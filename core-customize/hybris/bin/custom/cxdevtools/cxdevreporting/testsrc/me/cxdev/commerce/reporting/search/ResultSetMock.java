package me.cxdev.commerce.reporting.search;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ResultSetMock implements ResultSet {
	private List<GenericSearchResultHeader> headerValues;
	private List<Map<GenericSearchResultHeader, Object>> rowValues;

	private boolean matchLabel;
	private boolean matchName;
	private int currentRow = -1;

	public void withoutMatchingHeader() {
		matchLabel = false;
		matchName = false;
	}

	public void withMatchingHeaderAll() {
		matchLabel = true;
		matchName = true;
	}

	public void withMatchingHeaderLabel() {
		withoutMatchingHeader();
		matchLabel = true;
	}

	public void withMatchingHeaderName() {
		withoutMatchingHeader();
		matchName = true;
	}

	public ResultSetMock(List<GenericSearchResultHeader> headerValues, List<Map<GenericSearchResultHeader, Object>> rowValues) {
		this.headerValues = headerValues;
		this.rowValues = rowValues;
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return new ResultSetMetaDataMock(this.headerValues);
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		if (matchLabel) {
			GenericSearchResultHeader headerFromLabel = headerValues.stream()
					.filter(Objects::nonNull)
					.filter(h -> columnLabel.equals(h.getColumnLabel()))
					.findFirst()
					.orElseThrow(() -> new SQLException("Could not identify column with label: " + columnLabel));
			return headerFromLabel.getColumnIndex();
		}

		if (matchName) {
			GenericSearchResultHeader headerFromName = headerValues.stream()
					.filter(Objects::nonNull)
					.filter(h -> columnLabel.equals(h.getColumnName()))
					.findFirst()
					.orElseThrow(() -> new SQLException("Could not identify column with name: " + columnLabel));
			return headerFromName.getColumnIndex();
		}

		throw new SQLException("Could not identify column with label or name: " + columnLabel);
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		GenericSearchResultHeader header = headerValues.stream()
				.filter(Objects::nonNull)
				.filter(h -> h.getColumnIndex() == columnIndex)
				.findFirst()
				.orElseThrow(() -> new SQLException("Could not find column with index: " + columnIndex));
		return rowValues.get(currentRow).get(header);
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getObject(findColumn(columnLabel));
	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		return (T) getObject(columnIndex);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		return getObject(findColumn(columnLabel), type);
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		return String.valueOf(getObject(columnIndex));
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return getString(findColumn(columnLabel));
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		return Boolean.parseBoolean(getString(columnIndex));
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		return getBoolean(findColumn(columnLabel));
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		return Byte.parseByte(getString(columnIndex));
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		return getByte(findColumn(columnLabel));
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		return Short.parseShort(getString(columnIndex));
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		return getShort(findColumn(columnLabel));
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		return Integer.parseInt(getString(columnIndex));
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		return getInt(findColumn(columnLabel));
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		return Long.parseLong(getString(columnIndex));
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		return getLong(findColumn(columnLabel));
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		return Float.parseFloat(getString(columnIndex));
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		return getFloat(findColumn(columnLabel));
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		return Double.parseDouble(getString(columnIndex));
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		return getDouble(findColumn(columnLabel));
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return new BigDecimal(getString(columnIndex));
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return getBigDecimal(findColumn(columnLabel));
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		return getBigDecimal(columnIndex).setScale(scale);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		return getBigDecimal(findColumn(columnLabel), scale);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		return getString(columnIndex).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		return getBytes(findColumn(columnLabel));
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		return Date.valueOf(getString(columnIndex));
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return getDate(columnIndex);
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return getDate(findColumn(columnLabel));
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		return getDate(findColumn(columnLabel), cal);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		return Time.valueOf(getString(columnIndex));
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return getTime(findColumn(columnLabel));
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return getTime(columnIndex);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		return getTime(findColumn(columnLabel), cal);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return Timestamp.valueOf(getString(columnIndex));
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return getTimestamp(findColumn(columnLabel));
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		return getTimestamp(columnIndex);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		return getTimestamp(findColumn(columnLabel), cal);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		try {
			return new URL(getString(columnIndex));
		} catch (MalformedURLException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		return getURL(findColumn(columnLabel));
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		String value = getString(columnIndex);
		return (value == null) ? null : new ByteArrayInputStream(value.getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		return getAsciiStream(findColumn(columnLabel));
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		String value = getString(columnIndex);
		return (value == null) ? null : new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		return getUnicodeStream(findColumn(columnLabel));
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return getUnicodeStream(columnIndex);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		return getBinaryStream(findColumn(columnLabel));
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return new StringReader(getString(columnIndex));
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		return getCharacterStream(findColumn(columnLabel));
	}

	@Override
	public int getRow() throws SQLException {
		return currentRow;
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		return getRow() < 0;
	}

	@Override
	public boolean isFirst() throws SQLException {
		return getRow() == 0;
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		return getRow() >= rowValues.size();
	}

	@Override
	public boolean isLast() throws SQLException {
		return getRow() == rowValues.size() - 1;
	}

	@Override
	public void beforeFirst() throws SQLException {
		currentRow = -1;
	}

	@Override
	public boolean first() throws SQLException {
		return absolute(0);
	}

	@Override
	public boolean previous() throws SQLException {
		return relative(-1);
	}

	@Override
	public boolean next() throws SQLException {
		return relative(1);
	}

	@Override
	public boolean last() throws SQLException {
		return absolute(rowValues.size() - 1);
	}

	@Override
	public void afterLast() throws SQLException {
		currentRow = rowValues.size();
	}

	@Override
	public boolean relative(int rows) throws SQLException {
		int finalPosition = getRow() + rows;
		if (0 <= finalPosition && finalPosition < rowValues.size()) {
			currentRow = finalPosition;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		if (0 <= row && row < rowValues.size()) {
			currentRow = row;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int getType() throws SQLException {
		return TYPE_SCROLL_SENSITIVE;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return FETCH_FORWARD;
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
	}

	@Override
	public int getFetchSize() throws SQLException {
		return rowValues.size();
	}

	@Override
	public int getConcurrency() throws SQLException {
		return CONCUR_READ_ONLY;
	}

	@Override
	public int getHoldability() throws SQLException {
		return CLOSE_CURSORS_AT_COMMIT;
	}

	@Override
	public boolean isClosed() throws SQLException {
		return false;
	}

	@Override
	public void close() throws SQLException {
	}

	@Override
	public Statement getStatement() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public boolean wasNull() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		return null;
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void refreshRow() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void insertRow() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public boolean rowInserted() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void updateRow() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void deleteRow() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public String getCursorName() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public void clearWarnings() throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException("Operation is not supported by mock!");
	}
}
