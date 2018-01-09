package org.frameworkset.elasticsearch.event;

import java.util.Map;

public class BaseEvent {
	protected String id;
	protected Map<String, String> headers;
	protected Long TTL;
	protected String indexType;
	protected String indexPrefix;
	protected long timestamp;
	protected String indexTimestamp;
	public BaseEvent() {
		// TODO Auto-generated constructor stub
	}
	  
	  public Map<String, String> getHeaders() {
	    return headers;
	  }


	public String getIndexTimestamp() {
		return indexTimestamp;
	}

	public void setIndexTimestamp(String indexTimestamp) {
		this.indexTimestamp = indexTimestamp;
	}

	public void setHeaders(Map<String, String> headers) {
	    this.headers = headers;
	  }

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getIndexType() {
			return indexType;
		}
		
		public void setIndexType(String indexType) {
			this.indexType = indexType;
		}
		
		public Long getTTL() {
			return TTL;
		}
		
		public void setTTL(Long tTL) {
			TTL = tTL;
		}
		
		public String getIndexPrefix() {
			// TODO Auto-generated method stub
			return indexPrefix;
		}
		
		public void setIndexPrefix(String indexPrefix) {
			this.indexPrefix = indexPrefix;
			
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	 
}
