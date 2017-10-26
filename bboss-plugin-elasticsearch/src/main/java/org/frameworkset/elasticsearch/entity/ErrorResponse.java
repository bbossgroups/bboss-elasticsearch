package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse implements SearchResult,Serializable{
	private ErrorInfo error;
	private int status;
	public ErrorResponse() {
		// TODO Auto-generated constructor stub
	}
	public ErrorInfo getError() {
		return error;
	}
	public void setError(ErrorInfo error) {
		this.error = error;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}

}
