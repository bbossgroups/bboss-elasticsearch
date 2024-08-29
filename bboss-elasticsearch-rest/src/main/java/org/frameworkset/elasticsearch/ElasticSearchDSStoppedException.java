package org.frameworkset.elasticsearch;

public class ElasticSearchDSStoppedException extends ElasticSearchException {
  
    public ElasticSearchDSStoppedException() {
    }

    public ElasticSearchDSStoppedException(String message) {
        super(message);
    }

    public ElasticSearchDSStoppedException(Throwable cause) {
        super(cause);
    }

    public ElasticSearchDSStoppedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ElasticSearchDSStoppedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
