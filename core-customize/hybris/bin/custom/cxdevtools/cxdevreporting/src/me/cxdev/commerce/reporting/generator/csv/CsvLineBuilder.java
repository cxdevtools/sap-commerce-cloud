package me.cxdev.commerce.reporting.generator.csv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CsvLineBuilder {
	private final Map<String, String> fields;
	protected final Map<String, String> lineValues;

	public CsvLineBuilder(Map<String, String> fields) {
		this.fields = fields;
		lineValues = new HashMap<>(fields.size());
	}

	public void addValue(String column, String value) {
		if (fields.containsKey(column)) {
			lineValues.put(column, value);
		}
	}

	public void addAllValues(Map<String, String> values) {
		lineValues.putAll(values);
	}

	public Map<Integer, String> header() {
		lineValues.clear();
		lineValues.putAll(fields);
		return build();
	}

	public Map<Integer, String> build() {
		Map<Integer, String> result = new HashMap<>(fields.size());

		Iterator<String> keys = fields.keySet().iterator();
		for (int i = 0; keys.hasNext(); i++) {
			String key = keys.next();
			if (lineValues.containsKey(key)) {
				result.put(i, lineValues.get(key));
			}
		}

		lineValues.clear();
		return result;
	}
}
