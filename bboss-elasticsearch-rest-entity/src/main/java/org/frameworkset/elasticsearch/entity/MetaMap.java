package org.frameworkset.elasticsearch.entity;
/**
 * Copyright 2008 biaoping.yin
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: 包含文档元数据的Map对象</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/9/12 11:35
 * @author biaoping.yin
 * @version 1.0
 */
public class MetaMap extends HashMap {
	/**文档_id*/
	private String id;
	/**文档对应索引类型信息*/
	private String  type;
	/**文档对应索引字段信息*/
	private Map<String, List<Object>> fields;
/**文档对应版本信息*/
	private long version;
 /**文档对应的索引名称*/
	private String index;
 /**文档对应的高亮检索信息*/
	private Map<String,List<Object>> highlight;
	 /**文档对应的排序信息*/
	private Object[] sort;
	 /**文档对应的评分信息*/
	private Double  score;
	 /**文档对应的父id*/
	private Object parent;
	 /**文档对应的路由信息*/
	private Object routing;
	 /**文档对应的是否命中信息*/
	private boolean found;
	 /**文档对应的nested检索信息*/
	private Map<String,Object> nested;
	 /**文档对应的innerhits信息*/
	private Map<String,Map<String, InnerSearchHits>> innerHits;
	 /**文档对应的索引分片号*/
	private String shard;
	 /**文档对应的elasticsearch集群节点名称*/
	private String node;
	/**文档对应的打分规则信息*/
	private Explanation explanation;

	private long seqNo;//"_index": "trace-2017.09.01",
	private long primaryTerm;//"_index": "trace-2017.09.01",

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, List<Object>> getFields() {
		return fields;
	}

	public void setFields(Map<String, List<Object>> fields) {
		this.fields = fields;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public Map<String, List<Object>> getHighlight() {
		return highlight;
	}

	public void setHighlight(Map<String, List<Object>> highlight) {
		this.highlight = highlight;
	}

	public Object[] getSort() {
		return sort;
	}

	public void setSort(Object[] sort) {
		this.sort = sort;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public Object getParent() {
		return parent;
	}

	public void setParent(Object parent) {
		this.parent = parent;
	}

	public Object getRouting() {
		return routing;
	}

	public void setRouting(Object routing) {
		this.routing = routing;
	}

	public boolean isFound() {
		return found;
	}

	public void setFound(boolean found) {
		this.found = found;
	}

	public Map<String, Object> getNested() {
		return nested;
	}

	public void setNested(Map<String, Object> nested) {
		this.nested = nested;
	}

	public Map<String, Map<String, InnerSearchHits>> getInnerHits() {
		return innerHits;
	}

	public void setInnerHits(Map<String, Map<String, InnerSearchHits>> innerHits) {
		this.innerHits = innerHits;
	}

	public String getShard() {
		return shard;
	}

	public void setShard(String shard) {
		this.shard = shard;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public Explanation getExplanation() {
		return explanation;
	}

	public void setExplanation(Explanation explanation) {
		this.explanation = explanation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(long seqNo) {
		this.seqNo = seqNo;
	}

	public long getPrimaryTerm() {
		return primaryTerm;
	}

	public void setPrimaryTerm(long primaryTerm) {
		this.primaryTerm = primaryTerm;
	}
}
