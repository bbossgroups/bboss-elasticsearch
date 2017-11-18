package org.frameworkset.elasticsearch.event;

import org.elasticsearch.common.xcontent.XContentType;

import java.util.Map;

public interface Event {

	  /**
	   * Returns a map of name-value pairs describing the data stored in the body.
	   */
	  public Map<String, String> getHeaders();

	  /**
	   * Set the event headers
	   * @param headers Map of headers to replace the current headers.
	   */
	  public void setHeaders(Map<String, String> headers);

	  /**
	   * Returns the raw byte array of the data contained in this event.
	   */
	  public Object getBody();

	  /**
	   * Sets the raw byte array of the data contained in this event.
	   * @param body The data.
	   */
	  public void setBody(Object body);
	  
	  public void setIndexType(String indexType);
	  public String getIndexType();
	  public void setTTL(Long ttl);
	  public Long getTTL();
	  public String getIndexPrefix();
	  public void setIndexPrefix(String indexPrefix);
	  public void setId(String id);
	  public String getId();
	  public XContentType getXContentType();
	  public void setXContentType(XContentType xcontentType);
}
