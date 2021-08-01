/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.frameworkset.elasticsearch.client;

import org.frameworkset.elasticsearch.ElasticSearch;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Interface for an ElasticSearch client which is responsible for sending bulks
 * of events to ElasticSearch.
 */
public interface ElasticSearchClient {
	public void recoverRemovedNodes(List<ESAddress> hosts);
	public boolean containAddress(ESAddress address);
	public void addAddresses(List<ESAddress> address);
	public void handleRemoved(List<ESAddress> hosts);
	void configure(Properties elasticsearchPropes);

	/**
	 * Close connection to elastic search in client
	 */
	void close();
	ClientUtil getClientUtil(IndexNameBuilder indexNameBuilder);
	public ClientUtil getConfigClientUtil(IndexNameBuilder indexNameBuilder,String configFile);
	public ClientUtil getConfigClientUtil(IndexNameBuilder indexNameBuilder, BaseTemplateContainerImpl templateContainer);

	public void init();

	public Map getClusterInfo() ;
	public String getClusterVarcharInfo();
	public String getClusterVersionInfo();
	public boolean isV1();
	public Integer slowDslThreshold();
	public LogDslCallback getSlowDslCallback();
	public LogDslCallback getLogDslCallback();
	public ElasticSearch getElasticSearch();

	void setShowTemplate(boolean showdsl);
//	BulkRequestBuilder prepareBulk();
//
//	IndexRequestBuilder createIndexRequest(IndexNameBuilder indexNameBuilder, Event event) throws IOException;
}
