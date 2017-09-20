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

import org.frameworkset.elasticsearch.IndexNameBuilder;

import java.util.Properties;

/**
 * Interface for an ElasticSearch client which is responsible for sending bulks
 * of events to ElasticSearch.
 */
public interface ElasticSearchClient {
	void configure(Properties elasticsearchPropes);

	/**
	 * Close connection to elastic search in client
	 */
	void close();
	ClientUtil getClientUtil(IndexNameBuilder indexNameBuilder);
	public ClientUtil getConfigClientUtil(IndexNameBuilder indexNameBuilder,String configFile);
//	BulkRequestBuilder prepareBulk();
//
//	IndexRequestBuilder createIndexRequest(IndexNameBuilder indexNameBuilder, Event event) throws IOException;
}
