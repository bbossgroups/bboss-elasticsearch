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
package org.frameworkset.elasticsearch;

import org.frameworkset.elasticsearch.handler.ElasticSearchMapResponseHandler;

public class ElasticSearchSinkConstants {

  /**
   * Comma separated list of hostname:port, if the port is not present the
   * default port '9300' will be used</p>
   * Example:
   * <pre>
   *  127.0.0.1:92001,127.0.0.2:9300
   * </pre>
   */
  public static final String REST_HOSTNAMES = "elasticsearch.rest.hostNames";
  public static final String TRANSPORT_HOSTNAMES = "elasticsearch.transport.hostNames";
  public static final String healthCheckHttpPool = "esHealthCheckHttpPool";
  public static final ElasticSearchMapResponseHandler elasticSearchMapResponseHandler = new ElasticSearchMapResponseHandler();
  /**
   * The name to index the document to, defaults to 'flume'</p>
   * The current date in the format 'yyyy-MM-dd' will be appended to this name,
   * for example 'foo' will result in a daily index of 'foo-yyyy-MM-dd'
   */
  public static final String INDEX_NAME = "elasticsearch.indexName";

  /**
   * The type to index the document to, defaults to 'log'
   */
  public static final String INDEX_TYPE = "elasticsearch.indexType";

  /**
   * Name of the ElasticSearch cluster to connect to
   */
  public static final String CLUSTER_NAME = "elasticsearch.clusterName";

  /**
   * Maximum number of events the sink should take from the channel per
   * transaction, if available. Defaults to 100
   */
  public static final String BATCH_SIZE = "batchSize";

  /**
   * TTL in days, when set will cause the expired documents to be deleted
   * automatically, if not set documents will never be automatically deleted
   */
  public static final String TTL = "elasticsearch.ttl";

  /**
   * The fully qualified class name of the serializer the sink should use.
   */
  public static final String SERIALIZER = "elasticsearch.serialize";

  /**
   * Configuration to pass to the serializer.
   */
  public static final String SERIALIZER_PREFIX = SERIALIZER + ".";

  /**
   * The fully qualified class name of the index name builder the sink
   * should use to determine name of index where the event should be sent.
   */
  public static final String INDEX_NAME_BUILDER = "indexNameBuilder";

  /**
   * The fully qualified class name of the index name builder the sink
   * should use to determine name of index where the event should be sent.
   */
  public static final String INDEX_NAME_BUILDER_PREFIX
          = INDEX_NAME_BUILDER + ".";

  /**
   * The client type used for sending bulks to ElasticSearch
   */
  public static final String CLIENT_TYPE = "elasticsearch.client";

  /**
   * The client type used for sending bulks to ElasticSearch
   */
  public static final String CLIENT_sliceScrollThreadCount = "elasticsearch.sliceScrollThreadCount";
  public static final String CLIENT_sliceScrollThreadQueue = "elasticsearch.sliceScrollThreadQueue";
  public static final String CLIENT_sliceScrollBlockedWaitTimeout = "elasticsearch.sliceScrollBlockedWaitTimeout";


  /**
   * The client prefix to extract the configuration that will be passed to
   * elasticsearch client.
   */
  public static final String CLIENT_PREFIX = CLIENT_TYPE + ".";

  /**
   * DEFAULTS USED BY THE SINK
   */

  public static final int DEFAULT_PORT = 9300;
  public static final int DEFAULT_TTL = -1;
  public static final String DEFAULT_INDEX_NAME = "trace";
  public static final String DEFAULT_INDEX_TYPE = "log";
  public static final String DEFAULT_CLUSTER_NAME = "elasticsearch";
  public static final String DEFAULT_CLIENT_TYPE = "transport";
  public static final String DEFAULT_USER = "elastic";
  public static final String DEFAULT_PASSWORD = "changeme";
  public static final String TTL_REGEX = "^(\\d+)(\\D*)";
  public static final String DEFAULT_SERIALIZER_CLASS = "org.frameworkset.elasticsearch.ElasticSearchJSONEventSerializer" ;
  public static final String DEFAULT_INDEX_NAME_BUILDER_CLASS =
          "org.frameworkset.elasticsearch.TimeBasedIndexNameBuilder";
}
