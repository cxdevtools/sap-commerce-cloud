package tools.sapcx.commerce.reporting.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class GenericSearchResultHeader {
	private int columnIndex;
	private String columnName;
	private String columnLabel;

	public GenericSearchResultHeader(int columnIndex, String columnName, String columnLabel) {
		this.columnIndex = columnIndex;
		this.columnName = columnName;
		this.columnLabel = columnLabel;
	}

	/**
	 * Note: the index of the result set is one-based!
	 *
	 * @return column index of database result set
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * Note: the name of a functional column is the one specified in the query by the "AS" operator or by the function itself.
	 *
	 * @return the name of the column of the database
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the label of the column as specified in the query by the "AS" operator
	 */
	public String getColumnLabel() {
		return columnLabel;
	}

	/**
	 * The exported name of this header column in order: label, name, index.
	 */
	public String getExportName() {
		if (StringUtils.isNotBlank(getColumnLabel())) {
			return getColumnLabel();
		} else if (StringUtils.isNotBlank(getColumnName())) {
			return getColumnName();
		} else {
			return String.valueOf(getColumnIndex());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		GenericSearchResultHeader that = (GenericSearchResultHeader) o;
		return new EqualsBuilder().append(columnIndex, that.columnIndex).append(columnName, that.columnName).append(columnLabel, that.columnLabel).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(columnIndex).append(columnName).append(columnLabel).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("columnIndex", columnIndex)
				.append("name", columnName)
				.append("label", columnLabel)
				.toString();
	}
}
