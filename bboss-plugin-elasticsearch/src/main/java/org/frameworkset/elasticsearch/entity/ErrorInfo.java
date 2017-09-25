package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ErrorInfo {
	@JsonProperty("root_cause")
	private List<RootCause> rootCauses;
	private String type;//": "search_phase_execution_exception",
	private String reason;//: "all shards failed",
	private String phase;//": "query",
	private boolean  grouped;//": true,
	private int line;
	private Map<String,Object> header;
	private int col;
	@JsonProperty("failed_shards")
    private List<FailedShard> failedShards;
	public ErrorInfo() {
		// TODO Auto-generated constructor stub
	}
	public List<RootCause> getRootCauses() {
		return rootCauses;
	}
	public void setRootCauses(List<RootCause> rootCauses) {
		this.rootCauses = rootCauses;
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
	public String getPhase() {
		return phase;
	}
	public void setPhase(String phase) {
		this.phase = phase;
	}
	public boolean isGrouped() {
		return grouped;
	}
	public void setGrouped(boolean grouped) {
		this.grouped = grouped;
	}
	public List<FailedShard> getFailedShards() {
		return failedShards;
	}
	public void setFailedShards(List<FailedShard> failedShards) {
		this.failedShards = failedShards;
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
