package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * 索引文档字段信息
 */
public class IndexField implements Serializable {
	private String type;
	private String fieldName;
	private Object ignoreAbove;
	private String format;

	private String analyzer;
	private String 		normalizer;
	private Object 		boost;
	private Boolean 		coerce;
	private String 		copyTo;
	private Boolean 		docValues;
	private Boolean 		dynamic;
	private Boolean 		enabled;
	private Boolean 		fielddata;
	private Boolean 		ignoreMalformed;
	private Boolean 		includeInAll;
	private String 		indexOptions;
	private Boolean 		index;
	private Map<String,Object> fields;
	private Boolean 		norms;
	private Object 		nullValue;
	private Object 		positionIncrementGap;
	private String 		searchAnalyzer;
	private String 		similarity;
	private Boolean 		store;
	private String 		termVector;
	private Map<String,Object> fielddataFrequencyFilter;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(String analyzer) {
		this.analyzer = analyzer;
	}

	public String getNormalizer() {
		return normalizer;
	}

	public void setNormalizer(String normalizer) {
		this.normalizer = normalizer;
	}

	public Object getBoost() {
		return boost;
	}

	public void setBoost(Object boost) {
		this.boost = boost;
	}

	public Boolean isCoerce() {
		return coerce;
	}

	public void setCoerce(Boolean coerce) {
		this.coerce = coerce;
	}

	public String getCopyTo() {
		return copyTo;
	}

	public void setCopyTo(String copyTo) {
		this.copyTo = copyTo;
	}

	public Boolean isDocValues() {
		return docValues;
	}

	public void setDocValues(Boolean docValues) {
		this.docValues = docValues;
	}

	public Boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(Boolean dynamic) {
		this.dynamic = dynamic;
	}

	public Boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean isFielddata() {
		return fielddata;
	}

	public void setFielddata(Boolean fielddata) {
		this.fielddata = fielddata;
	}

	public Boolean isIgnoreMalformed() {
		return ignoreMalformed;
	}

	public void setIgnoreMalformed(Boolean ignoreMalformed) {
		this.ignoreMalformed = ignoreMalformed;
	}

	public Boolean isIncludeInAll() {
		return includeInAll;
	}

	public void setIncludeInAll(Boolean includeInAll) {
		this.includeInAll = includeInAll;
	}

	public String getIndexOptions() {
		return indexOptions;
	}

	public void setIndexOptions(String indexOptions) {
		this.indexOptions = indexOptions;
	}

	public Boolean isIndex() {
		return index;
	}

	public void setIndex(Boolean index) {
		this.index = index;
	}

	public Map<String, Object> getFields() {
		return fields;
	}

	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}

	public Boolean isNorms() {
		return norms;
	}

	public void setNorms(Boolean norms) {
		this.norms = norms;
	}

	public Object getNullValue() {
		return nullValue;
	}

	public void setNullValue(Object nullValue) {
		this.nullValue = nullValue;
	}

	public Object getPositionIncrementGap() {
		return positionIncrementGap;
	}

	public void setPositionIncrementGap(Object positionIncrementGap) {
		this.positionIncrementGap = positionIncrementGap;
	}

	public String getSearchAnalyzer() {
		return searchAnalyzer;
	}

	public void setSearchAnalyzer(String searchAnalyzer) {
		this.searchAnalyzer = searchAnalyzer;
	}

	public String getSimilarity() {
		return similarity;
	}

	public void setSimilarity(String similarity) {
		this.similarity = similarity;
	}

	public Boolean isStore() {
		return store;
	}

	public void setStore(Boolean store) {
		this.store = store;
	}

	public String getTermVector() {
		return termVector;
	}

	public void setTermVector(String termVector) {
		this.termVector = termVector;
	}

	public Map<String, Object> getFielddataFrequencyFilter() {
		return fielddataFrequencyFilter;
	}

	public void setFielddataFrequencyFilter(Map<String, Object> fielddataFrequencyFilter) {
		this.fielddataFrequencyFilter = fielddataFrequencyFilter;
	}

	private Map<String,Object> properties;
	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Object getIgnoreAbove() {
		return ignoreAbove;
	}

	public void setIgnoreAbove(Object ignoreAbove) {
		this.ignoreAbove = ignoreAbove;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public Boolean getCoerce() {
		return coerce;
	}

	public Boolean getDocValues() {
		return docValues;
	}

	public Boolean getDynamic() {
		return dynamic;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public Boolean getFielddata() {
		return fielddata;
	}

	public Boolean getIgnoreMalformed() {
		return ignoreMalformed;
	}

	public Boolean getIncludeInAll() {
		return includeInAll;
	}

	public Boolean getIndex() {
		return index;
	}

	public Boolean getNorms() {
		return norms;
	}

	public Boolean getStore() {
		return store;
	}
}
