package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shards  implements Serializable {
	private long total;
	private long successful;
	private long failed;
	private List<Map<String,Object>> failures;
	private long skipped;
	public Shards() {
		// TODO Auto-generated constructor stub
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public long getSuccessful() {
		return successful;
	}
	public void setSuccessful(long successful) {
		this.successful = successful;
	}
	public long getFailed() {
		return failed;
	}
	public void setFailed(long failed) {
		this.failed = failed;
	}

	public long getSkipped() {
		return skipped;
	}

	public void setSkipped(long skipped) {
		this.skipped = skipped;
	}
	public List<Map<String, Object>> getFailures() {
		return failures;
	}
	public void setFailures(List<Map<String, Object>> failures) {
		this.failures = failures;
	}
	
}
