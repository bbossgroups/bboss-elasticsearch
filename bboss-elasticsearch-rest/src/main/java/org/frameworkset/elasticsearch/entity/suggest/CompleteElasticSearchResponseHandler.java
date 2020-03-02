package org.frameworkset.elasticsearch.entity.suggest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.RestResponse;
import org.frameworkset.elasticsearch.handler.ElasticSearchResponseHandler;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CompleteElasticSearchResponseHandler extends ElasticSearchResponseHandler {
	private static Logger logger = LoggerFactory.getLogger(CompleteElasticSearchResponseHandler.class);

	public CompleteElasticSearchResponseHandler() {
		// TODO Auto-generated constructor stub
	}

	public CompleteElasticSearchResponseHandler(ESTypeReferences<?,?> types) {
		super(types);
	}
	public CompleteElasticSearchResponseHandler(ESClassType type) {
		super(type);
	}

	public CompleteElasticSearchResponseHandler(Class<?> type) {
		super(type);
	}

	 @Override
     public RestResponse handleResponse(final HttpResponse response)
             throws ClientProtocolException, IOException {
         int status = response.getStatusLine().getStatusCode();

         if (status >= 200 && status < 300) {
             HttpEntity entity = response.getEntity();
			 CompleteRestResponse searchResponse = null;
              
             if (entity != null ) {
	             try {	            	
	            	 ESSerialThreadLocal.setESTypeReferences(types);
	            	 return super.converJson(entity,CompleteRestResponse.class);
//	                 searchResponse = SimpleStringUtil.json2Object(entity.getContent(), RestResponse.class) ;
	             }
	             catch (Exception e){
//					 throw new ElasticSearchException(e);
					 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e);
	             }
	             finally{
	            	 ESSerialThreadLocal.clean();
	             }
             }

             return searchResponse;

         } else {
             HttpEntity entity = response.getEntity();
//             if (entity != null ) {
//            	 throw new ElasticSearchException(EntityUtils.toString(entity));
////				 String content = EntityUtils.toString(entity);
////                 ErrorResponse searchResponse = null;
////                 try {
////                     searchResponse = entity != null ? SimpleStringUtil.json2Object(content, ErrorResponse.class) : null;
////                 }
////                 catch (Exception e){
////					 throw new ElasticSearchException(content,e);
////                 }
////                 return searchResponse;
//             }
//             else
//                 throw new ElasticSearchException("Unexpected response status: " + status);
			 if (entity != null )
				 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",").append(EntityUtils.toString(entity)).toString());
			 else
				 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).append(",Unexpected response status: ").append( status).toString());
         }
     }

}
