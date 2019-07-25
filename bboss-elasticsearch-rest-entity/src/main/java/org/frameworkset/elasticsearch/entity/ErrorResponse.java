package org.frameworkset.elasticsearch.entity;

import java.io.Serializable;

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
