package org.frameworkset.elasticsearch.handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.MapRestResponse;
import org.frameworkset.spi.remote.http.BaseResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticSearchMapResponseHandler extends BaseResponseHandler implements ResponseHandler<MapRestResponse> {
	private static Logger logger = LoggerFactory.getLogger(ElasticSearchMapResponseHandler.class);

	public ElasticSearchMapResponseHandler() {
		// TODO Auto-generated constructor stub
	}



	 @Override
     public MapRestResponse handleResponse(final HttpResponse response)
             throws ClientProtocolException, IOException {
         int status = response.getStatusLine().getStatusCode();

         if (status >= 200 && status < 300) {
             HttpEntity entity = response.getEntity();


             if (entity != null ) {

	             try {

					 return super.converJson(entity,MapRestResponse.class);
	             }
	             catch (Exception e){
					 throw new ElasticSearchException(e,status);
	             }

             }

             return null;

         } else {
             HttpEntity entity = response.getEntity();
             if (entity != null ) {
            	 throw new ElasticSearchException(EntityUtils.toString(entity),status);
//				 String content = EntityUtils.toString(entity);
//                 ErrorResponse searchResponse = null;
//                 try {
//                     searchResponse = entity != null ? SimpleStringUtil.json2Object(content, ErrorResponse.class) : null;
//                 }
//                 catch (Exception e){
//					 throw new ElasticSearchException(content,e);
//                 }
//                 return searchResponse;
             }
             else
                 throw new ElasticSearchException("Unexpected response status: " + status,status);
         }
     }

}
