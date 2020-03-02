package org.frameworkset.elasticsearch.handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.MapRestResponse;
import org.frameworkset.spi.remote.http.URLResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticSearchMapResponseHandler extends BaseExceptionResponseHandler implements URLResponseHandler<MapRestResponse> {
	private static Logger logger = LoggerFactory.getLogger(ElasticSearchMapResponseHandler.class);

	public ElasticSearchMapResponseHandler() {
		// TODO Auto-generated constructor stub
	}



	 @Override
     public MapRestResponse handleResponse(final HttpResponse response)
             throws ClientProtocolException, IOException {
		 int status = initStatus(  response);

         if (status >= 200 && status < 300) {
             HttpEntity entity = response.getEntity();


             if (entity != null ) {

	             try {

					 return super.converJson(entity,MapRestResponse.class);
	             }
	             catch (Exception e){
					 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e,status);
	             }

             }

             return null;

         } else {
             HttpEntity entity = response.getEntity();
             return (MapRestResponse)super.handleException(url,entity,status);

//             if (entity != null ) {
//            	 throw new ElasticSearchException(EntityUtils.toString(entity),status);
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
//                 throw new ElasticSearchException("Unexpected response status: " + status,status);
         }
     }

}
