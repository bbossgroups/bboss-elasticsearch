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

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.frameworkset.elasticsearch.event.Event;

import com.frameworkset.util.SimpleStringUtil;

/**
 * Serialize flume events into the same format LogStash uses</p>
 *
 * This can be used to send events to ElasticSearch and use clients such as
 * Kabana which expect Logstash formated indexes
 *
 * <pre>
 * {
 *    "@timestamp": "2010-12-21T21:48:33.309258Z",
 *    "@tags": [ "array", "of", "tags" ],
 *    "@type": "string",
 *    "@source": "source of the event, usually a URL."
 *    "@source_host": ""
 *    "@source_path": ""
 *    "@fields":{
 *       # a set of fields for this event
 *       "user": "jordan",
 *       "command": "shutdown -r":
 *     }
 *     "@message": "the original plain-text message"
 *   }
 * </pre>
 *
 * If the following headers are present, they will map to the above logstash
 * output as long as the logstash fields are not already present.</p>
 *
 * <pre>
 *  timestamp: long -> @timestamp:Date
 *  host: String -> @source_host: String
 *  src_path: String -> @source_path: String
 *  type: String -> @type: String
 *  source: String -> @source: String
 * </pre>
 *
 * https
 *      ://github.com/logstash/logstash/wiki/logstash%27s-internal-message-
 *      format
 */
public class ElasticSearchJSONEventSerializer  implements
ElasticSearchEventSerializer {
  private   Map<String,Object> jsonConverter(Event event) throws IOException {
    Map<String,Object> builder = new HashMap<String,Object>();
    appendBody(builder, event);
    appendHeaders(builder, event);
    return builder;

  }

  @Override
  public   XContentBuilder getContentBuilder(Event event) throws IOException {
    XContentBuilder builder = jsonBuilder();
    Map<String, String> headers = event.getHeaders();
    
        Map<String,Object> map = jsonConverter(event);
        builder.map(map);
//        XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser( NamedXContentRegistry.EMPTY,data.getBytes(ContentBuilderUtil.charset));
//        parser.close();
//        builder.copyCurrentStructure(parser);

        return builder;
       
     

  }

  private   void appendBody(Map<String,Object> builder, Event event)
      throws IOException, UnsupportedEncodingException {
    String data = String.valueOf(event.getBody());
    if(data.startsWith("{") && data.endsWith("}")){
      Map<String,Object> body_ = SimpleStringUtil.json2Object(data,Map.class);
      builder.putAll(body_);
    }
    else{
      builder.put("@message",data);

    }



  }

  private   void appendHeaders(Map<String,Object> builder, Event event)
      throws IOException {
//    Map<String, String> headers = Maps.newHashMap(event.getHeaders());
	  MapBuilder mapBuilder = MapBuilder.newMapBuilder();
//	  mapBuilder.putAll(event.getHeaders());
//	  Map<String, String> headers = mapBuilder.map();
    Map<String, String> headers = event.getHeaders();
    if(headers == null || headers.size() == 0){
      return;
    }
    String timestamp = headers.remove("timestamp");
    if (!SimpleStringUtil.isEmpty(timestamp)
        && SimpleStringUtil.isEmpty(headers.get("@timestamp"))) {
      long timestampMs = Long.parseLong(timestamp);
      builder.put("@timestamp", new Date(timestampMs));
    }

    String source = headers.remove("source");
    if (!SimpleStringUtil.isEmpty(source)
        && SimpleStringUtil.isEmpty(headers.get("@source"))) {
      builder.put( "@source",
          source);
    }

    String type = headers.remove("type");
    if (!SimpleStringUtil.isEmpty(type)
        && SimpleStringUtil.isEmpty(headers.get("@type"))) {
      builder.put("@type", type);
    }

    String host = headers.remove("host");
    if (!SimpleStringUtil.isEmpty(host)
        && SimpleStringUtil.isEmpty(headers.get("@source_host"))) {

      builder.put( "beat.host",
          host);
    }

    String srcPath = headers.remove("src_path");
    if (!SimpleStringUtil.isEmpty(srcPath)
        && SimpleStringUtil.isEmpty(headers.get("@source_path"))) {
      builder.put( "@source_path",
          srcPath);
    }


    for (String key : headers.keySet()) {
      String val = headers.get(key);
      builder.put(key, val);
    }

  }

 
  public void configure(Properties elasticsearchPropes) {
    // NO-OP...
  }

 
}
