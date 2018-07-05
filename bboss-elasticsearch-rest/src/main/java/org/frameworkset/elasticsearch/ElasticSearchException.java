package org.frameworkset.elasticsearch;

public class ElasticSearchException extends RuntimeException {
	private int httpStatusCode = -1;
	public ElasticSearchException(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	public ElasticSearchException(String message,int httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}

	public ElasticSearchException(Throwable cause,int httpStatusCode) {
		super(cause);
		this.httpStatusCode = httpStatusCode;
	}

	public ElasticSearchException(String message, Throwable cause,int httpStatusCode) {
		super(message, cause);
		this.httpStatusCode = httpStatusCode;
	}

	public ElasticSearchException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace,int httpStatusCode) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.httpStatusCode = httpStatusCode;
	}

	public ElasticSearchException() {
	}

	public ElasticSearchException(String message) {
		super(message);
	}

	public ElasticSearchException(Throwable cause ) {
		super(cause);
	}

	public ElasticSearchException(String message, Throwable cause ) {
		super(message, cause);
	}

	public ElasticSearchException(String message, Throwable cause, boolean enableSuppression,
								  boolean writableStackTrace ) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}
}
