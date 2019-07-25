package org.frameworkset.elasticsearch.entity.geo;
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

import java.io.Serializable;

/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/geo-shape.html
 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/query-dsl-geo-shape-query.html
 * <p>Description: in GeoJSON and WKT, and therefore Elasticsearch, the correct coordinate order is longitude, latitude (X, Y) within coordinate arrays</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/7 18:08
 * @author biaoping.yin
 * @version 1.0
 */
public class GeoPoint implements Serializable {
	/**
	 * 经度
	 */
	private double lon;
	/**
	 * 纬度
	 */
	private double lat;


	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}
}
