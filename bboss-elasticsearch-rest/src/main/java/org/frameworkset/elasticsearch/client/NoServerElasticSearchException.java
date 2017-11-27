package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearchException;

public class NoServerElasticSearchException extends ElasticSearchException {
	public NoServerElasticSearchException() {
	}

	public NoServerElasticSearchException(String message) {
		super(message);
	}

	public NoServerElasticSearchException(Throwable cause) {
		super(cause);
	}

	public NoServerElasticSearchException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoServerElasticSearchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
