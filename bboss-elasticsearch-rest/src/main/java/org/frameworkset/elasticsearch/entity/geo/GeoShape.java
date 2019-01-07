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

/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/geo-shape.html
 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/query-dsl-geo-shape-query.html
 * <p>Description: in GeoJSON and WKT, and therefore Elasticsearch, the correct coordinate order is longitude, latitude (X, Y) within coordinate arrays</p>
 * <p></p>
 * <p>Copyright (c) 2018</p>
 * @Date 2019/1/7 18:14
 * @author biaoping.yin
 * @version 1.0
 */
public class GeoShape {
	/**
	 * type取值范围，参考文档：
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/geo-shape.html
	 */
	private String type;
	private String orientation;
	/**
	 *  一位数组或者二维数组,或者多维数组
	 *  in GeoJSON and WKT, and therefore Elasticsearch, the correct coordinate order is longitude, latitude (X, Y) within coordinate arrays
	 */
	private Object coordinates;

	/**
	 * type为circle时，指定半径
	 */
	private String radius;
}
