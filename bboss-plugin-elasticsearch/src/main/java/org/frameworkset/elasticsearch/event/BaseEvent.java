package org.frameworkset.elasticsearch.event;

import java.util.Map;

public class BaseEvent {
	protected String id;
	protected Map<String, String> headers;
	protected long TTL;
	protected String indexType;
	protected String indexPrefix;
	public BaseEvent() {
		// TODO Auto-generated constructor stub
	}
	  
	  public Map<String, String> getHeaders() {
	    return headers;
	  }

	  
	  public void setHeaders(Map<String, String> headers) {
	    this.headers = headers;
	  }

		public String getIndexType() {
			return indexType;
		}
		
		public void setIndexType(String indexType) {
			this.indexType = indexType;
		}
		
		public long getTTL() {
			return TTL;
		}
		
		public void setTTL(long tTL) {
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
