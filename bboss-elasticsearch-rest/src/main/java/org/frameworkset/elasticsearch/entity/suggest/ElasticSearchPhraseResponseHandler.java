package org.frameworkset.elasticsearch.entity.suggest;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.spi.remote.http.BaseResponseHandler;
import org.frameworkset.spi.remote.http.URLResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticSearchPhraseResponseHandler extends BaseResponseHandler<PhraseRestResponse>  implements URLResponseHandler<PhraseRestResponse> {
	private static Logger logger = LoggerFactory.getLogger(ElasticSearchPhraseResponseHandler.class);

	public ElasticSearchPhraseResponseHandler() {
		// TODO Auto-generated constructor stub
	}



	 @Override
     public PhraseRestResponse handleResponse(final ClassicHttpResponse response)
             throws ClientProtocolException, IOException, ParseException {
         int status = response.getCode();

         if (org.frameworkset.spi.remote.http.ResponseUtil.isHttpStatusOK( status)) {
             HttpEntity entity = response.getEntity();


             if (entity != null ) {

	             try {

					 return super.converJson(entity,PhraseRestResponse.class);
	             }
	             catch (Exception e){
					 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e);
	             }

             }

             return null;

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
