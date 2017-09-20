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

import java.util.Properties;
import java.util.TimeZone;

import org.frameworkset.elasticsearch.event.Event;
import org.frameworkset.util.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frameworkset.util.SimpleStringUtil;
import com.google.common.annotations.VisibleForTesting;

/**
 * Default index name builder. It prepares name of index using configured
 * prefix and current timestamp. Default format of name is prefix-yyyy-MM-dd".
 */
public class TimeBasedIndexNameBuilder implements
        IndexNameBuilder {
  private static final Logger logger = LoggerFactory
          .getLogger(TimeBasedIndexNameBuilder.class);
  public static final String DATE_FORMAT = "elasticsearch.dateFormat";
  public static final String TIME_ZONE = "elasticsearch.timeZone";

  public static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd";
  public static final String DEFAULT_TIME_ZONE = "";

  private FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy.MM.dd",
      TimeZone.getTimeZone("Etc/UTC"));

  private String indexPrefix;

  @VisibleForTesting
  FastDateFormat getFastDateFormat() {
    return fastDateFormat;
  }

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

  @Override
  public void configure(Properties elasticsearchPropes) {
    String dateFormatString = elasticsearchPropes.getProperty(DATE_FORMAT);

    String timeZoneString = elasticsearchPropes.getProperty(TIME_ZONE);
	//  logger.info(">>>>>>>>>>>>>>>>>>dateFormatString:"+dateFormatString+",timeZoneString:"+timeZoneString);
    if (SimpleStringUtil.isEmpty(dateFormatString)) {
      dateFormatString = DEFAULT_DATE_FORMAT;
    }
    if (SimpleStringUtil.isEmpty(timeZoneString)) {
      timeZoneString = DEFAULT_TIME_ZONE;
    }
    fastDateFormat = FastDateFormat.getInstance(dateFormatString,
        TimeZone.getTimeZone(timeZoneString));
    indexPrefix = elasticsearchPropes.getProperty(ElasticSearchSinkConstants.INDEX_NAME);
    logger.debug("dateFormatString = "+dateFormatString+",timeZoneString="+timeZoneString+",indexPrefix="+indexPrefix);
  }

  
}
