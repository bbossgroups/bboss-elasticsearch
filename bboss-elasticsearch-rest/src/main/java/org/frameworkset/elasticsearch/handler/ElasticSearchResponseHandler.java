package org.frameworkset.elasticsearch.handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.RestResponse;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticSearchResponseHandler extends BaseESResponsehandler {
	private static Logger logger = LoggerFactory.getLogger(ElasticSearchResponseHandler.class);
	
	public ElasticSearchResponseHandler() {
		// TODO Auto-generated constructor stub
	}
	
	public ElasticSearchResponseHandler(ESTypeReferences<?,?> types) {
		super(types);
	}
	public ElasticSearchResponseHandler(ESClassType type) {
		super(type);
	}

	public ElasticSearchResponseHandler(Class<?> type) {
		super(type);
	}

	 @Override
     public RestResponse handleResponse(final HttpResponse response)
             throws ClientProtocolException, IOException {
		 int status = initStatus(  response);

         if (status >= 200 && status < 300) {
             HttpEntity entity = response.getEntity();
             RestResponse searchResponse = null;
              
             if (entity != null ) {
	             try {	            	
	            	 ESSerialThreadLocal.setESTypeReferences(types);
	            	 return super.converJson(entity,RestResponse.class);
//	                 searchResponse = SimpleStringUtil.json2Object(entity.getContent(), RestResponse.class) ;
	             }
	             catch (Exception e){
					 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e,status);
	             }
	             finally{
	            	 ESSerialThreadLocal.clean();
	             }
             }

             return searchResponse;

         } else {
             HttpEntity entity = response.getEntity();
			 return (RestResponse)super.handleException(url,entity,status);

         }
     }

}
