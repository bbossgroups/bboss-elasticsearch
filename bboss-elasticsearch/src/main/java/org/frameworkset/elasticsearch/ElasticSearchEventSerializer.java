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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.elasticsearch.common.io.stream.BytesStream;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.frameworkset.elasticsearch.event.Event;

/**
 * Interface for an event serializer which serializes the headers and body of an
 * event to write them to ElasticSearch. This is configurable, so any config
 * params required should be taken through this.
 */
public interface ElasticSearchEventSerializer {

  public static final Charset charset = Charset.forName("UTF-8");

  /**
   * Return an {@link BytesStream} made up of the serialized flume event
   * @param event
   *          The flume event to serialize
   * @return A {@link BytesStream} used to write to ElasticSearch
   * @throws IOException
   *           If an error occurs during serialization
   */
  abstract   XContentBuilder getContentBuilder(Event event) throws IOException;
  public void configure(Properties elasticsearchPropes);
}
