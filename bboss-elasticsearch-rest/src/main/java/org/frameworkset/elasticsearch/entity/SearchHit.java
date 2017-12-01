package org.frameworkset.elasticsearch.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.frameworkset.elasticsearch.serial.ESHitDeserializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class SearchHit  extends BaseSearchHit{

	@JsonProperty("_source")
	private Object source;



	@JsonProperty("inner_hits")
	private Map<String,Map<String,InnerSearchHits>> innerHits;
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
	@JsonDeserialize(using = ESHitDeserializer.class)
	public void setSource(Object source) {
		this.source = source;
	}
	public Map<String, Map<String,InnerSearchHits>> getInnerHits() {
		return innerHits;
	}

	public void setInnerHits(Map<String, Map<String,InnerSearchHits>> innerHits) {
		this.innerHits = innerHits;
	}

	public <T> List<T> getInnerHits(String indexType, Class<T> type){
		if(this.innerHits == null || this.innerHits.size() == 0)
			return null;
		Map<String,InnerSearchHits> hits = this.innerHits.get(indexType);
		if(hits != null){
			InnerSearchHits ihits = hits.get("hits");
			if(ihits != null){
				List<InnerSearchHit> temp = ihits.getHits();
				if(temp.size() == 0)
					return null;
				if(InnerSearchHit.class.isAssignableFrom(type))
				{
					return (List<T>)temp;
				}
				else{
					List<T> ts = new ArrayList<T>(temp.size());
					for(int i = 0; i < temp.size(); i ++){
						ts.add((T) temp.get(i).getSource());
					}
					return ts;
				}
			}
		}
		return null;
	}
}
