package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public class RootCause  implements Serializable {
	private String type;//": "query_shard_exception",
	private String reason;//": "No mapping found for [start_time] in order to sort on",
	@JsonProperty("index_uuid")
	private String indexUuid;//": "Yh1s0aoDTdqa3ojpbYq2BQ",
	private String index;//": "trace-2017.08.31"
	private Map<String,Object> header;
	private int line;
	private int col;
	public RootCause() {
		// TODO Auto-generated constructor stub
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public String getIndexUuid() {
		return indexUuid;
	}
	public void setIndexUuid(String indexUuid) {
		this.indexUuid = indexUuid;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public int getCol() {
		return col;
	}
	public void setCol(int col) {
		this.col = col;
	}

	public Map<String, Object> getHeader() {
		return header;
	}

	public void setHeader(Map<String, Object> header) {
		this.header = header;
	}
}
