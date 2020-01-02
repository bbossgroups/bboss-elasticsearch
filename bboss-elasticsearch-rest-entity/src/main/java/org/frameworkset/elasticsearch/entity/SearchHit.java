package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.frameworkset.elasticsearch.serial.ESHitDeserializer;

import java.util.Map;
public class SearchHit  extends BaseSearchHit{

	@JsonProperty("_source")
	@JsonDeserialize(using = ESHitDeserializer.class)
	private Object source;




	public SearchHit() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 获取map类型的source对象
	 * @return
	 */
	public Map<String,Object> asMap(){
		if(source == null)
			return null;
		return (Map<String,Object>)source;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

}
