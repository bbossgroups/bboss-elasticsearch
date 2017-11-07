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

import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.event.Event;
import org.frameworkset.util.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

/**
 * Default index name builder. It prepares name of index using configured
 * prefix and current timestamp. Default format of name is prefix-yyyy-MM-dd".
 */
public class EventTimeBasedIndexNameBuilder extends TimeBasedIndexNameBuilder implements
        EventIndexNameBuilder {
  private static final Logger logger = LoggerFactory
          .getLogger(EventTimeBasedIndexNameBuilder.class);
  

 

  /**
   * Gets the name of the index to use for an index request
   * @param event
   *          Event for which the name of index has to be prepared
   * @return index name of the form 'indexPrefix-formattedTimestamp'
   */
  @Override
  public  String getIndexName(Event event) {
    TimestampedEvent timestampedEvent = new TimestampedEvent(event);
    long timestamp = timestampedEvent.getTimestamp();
    String realIndexPrefix = BucketPath.escapeString(event.getIndexPrefix() != null?event.getIndexPrefix():indexPrefix, event.getHeaders());
    String indexName = new StringBuilder(realIndexPrefix).append('-')
      .append(fastDateFormat.format(timestamp)).toString();
    logger.debug("Index Name = "+indexName);
    return indexName;
  }
 

  @Override
  public String getIndexPrefix(Event event) {
    return BucketPath.escapeString(event.getIndexPrefix() != null?event.getIndexPrefix():indexPrefix, event.getHeaders());
  }


  
}
