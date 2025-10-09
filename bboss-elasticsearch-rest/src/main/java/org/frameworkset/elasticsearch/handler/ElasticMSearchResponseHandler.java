package org.frameworkset.elasticsearch.handler;

import com.fasterxml.jackson.databind.JavaType;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.MSearchRestResponse;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.spi.remote.http.URLResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ElasticMSearchResponseHandler extends BaseResponsehandler<MSearchRestResponse> implements URLResponseHandler<MSearchRestResponse> {
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
     public MSearchRestResponse handleResponse(final ClassicHttpResponse response)
            throws ClientProtocolException, IOException, ParseException {
		 int status = initStatus(  response);

         if (org.frameworkset.spi.remote.http.ResponseUtil.isHttpStatusOK( status)) {
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
