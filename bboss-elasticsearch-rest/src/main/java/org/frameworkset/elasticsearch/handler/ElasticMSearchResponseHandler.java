package org.frameworkset.elasticsearch.handler;

import com.fasterxml.jackson.databind.JavaType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.MSearchRestResponse;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.spi.remote.http.URLResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticMSearchResponseHandler extends BaseResponsehandler implements URLResponseHandler<MSearchRestResponse> {
	private static Logger logger = LoggerFactory.getLogger(ElasticMSearchResponseHandler.class);

	public ElasticMSearchResponseHandler() {
		// TODO Auto-generated constructor stub
	}


    public ElasticMSearchResponseHandler(JavaType javaType) {
        super(javaType);
    }

    public ElasticMSearchResponseHandler(ESTypeReferences<?,?> types) {
        super(types);
    }
    public ElasticMSearchResponseHandler(ESClassType type) {
        super(type);
    }

    public ElasticMSearchResponseHandler(Class<?> types) {
        super(types);
    }



    @Override
     public MSearchRestResponse handleResponse(final HttpResponse response)
             throws ClientProtocolException, IOException {
		 int status = initStatus(  response);

         if (status >= 200 && status < 300) {
             HttpEntity entity = response.getEntity();

             if (entity != null ) {
	             try {	            	
	            	 ESSerialThreadLocal.setESTypeReferences(types);
	            	 return super.converJson(entity,MSearchRestResponse.class);
//	                 searchResponse = SimpleStringUtil.json2Object(entity.getContent(), RestResponse.class) ;
	             }
	             catch (Exception e){
					 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e,status);
	             }
	             finally{
	            	 ESSerialThreadLocal.clean();
	             }
             }

             return null;

         } else {
             HttpEntity entity = response.getEntity();
			 return (MSearchRestResponse)super.handleException(url,entity,status);

         }
     }

}
