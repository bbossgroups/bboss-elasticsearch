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
public class TimeBasedIndexNameBuilder implements
        IndexNameBuilder {
  private static final Logger logger = LoggerFactory
          .getLogger(TimeBasedIndexNameBuilder.class);
  public static final String DATE_FORMAT = "elasticsearch.dateFormat";
  public static final String TIME_ZONE = "elasticsearch.timeZone";

  public static final String DEFAULT_DATE_FORMAT = "yyyy.MM.dd";
  public static final String DEFAULT_TIME_ZONE = "";

  protected FastDateFormat fastDateFormat = FastDateFormat.getInstance("yyyy.MM.dd",
      TimeZone.getTimeZone("Etc/UTC"));

  protected String indexPrefix;

  public FastDateFormat getFastDateFormat() {
    return fastDateFormat;
  }

   

  @Override
  public String getIndexName(String index) {

    String indexName = new StringBuilder(index).append('-')
            .append(fastDateFormat.format(new Date())).toString();
    return indexName;
  }

   
  @Override
  public void configure(Properties elasticsearchPropes) {
    String dateFormatString = elasticsearchPropes.getProperty(DATE_FORMAT);

    String timeZoneString = elasticsearchPropes.getProperty(TIME_ZONE);
    indexPrefix = elasticsearchPropes.getProperty(ElasticSearchSinkConstants.INDEX_NAME);
    logger.info("dateFormatString = "+dateFormatString+",timeZoneString="+timeZoneString+",indexPrefix="+indexPrefix);
	//  logger.info(">>>>>>>>>>>>>>>>>>dateFormatString:"+dateFormatString+",timeZoneString:"+timeZoneString);

    if (SimpleStringUtil.isEmpty(dateFormatString) || dateFormatString.startsWith("${")) {

      dateFormatString = DEFAULT_DATE_FORMAT;
    }
    if (SimpleStringUtil.isEmpty(timeZoneString) || timeZoneString.startsWith("${")) {
      timeZoneString = DEFAULT_TIME_ZONE;
    }

    try {
      fastDateFormat = FastDateFormat.getInstance(dateFormatString,
              TimeZone.getTimeZone(timeZoneString));
    }
    catch (Exception e){
        logger.warn("解析时间格式异常[dateFormatString = "+dateFormatString+",timeZoneString="+timeZoneString+"],将采用默认的时间格式和时区[yyyy.MM.dd Etc/UTC]",e);
    }

  }

  
}
