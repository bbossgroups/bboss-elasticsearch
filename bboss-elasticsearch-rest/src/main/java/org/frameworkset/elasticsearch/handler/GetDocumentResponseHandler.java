package org.frameworkset.elasticsearch.handler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.entity.SearchHit;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESSerialThreadLocal;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GetDocumentResponseHandler extends BaseGetDocESResponsehandler {
	private static Logger logger = LoggerFactory.getLogger(GetDocumentResponseHandler.class);

	public GetDocumentResponseHandler() {
		// TODO Auto-generated constructor stub
	}

	public GetDocumentResponseHandler(ESTypeReferences<?,?> types) {
		super(types);
	}
	public GetDocumentResponseHandler(ESClassType type) {
		super(type);
	}

	public GetDocumentResponseHandler(Class<?> type) {
		super(type);
	}

	 @Override
     public SearchHit handleResponse(final HttpResponse response)
             throws ClientProtocolException, IOException {
		 int status = initStatus(  response);

         if (status >= 200 && status < 300) {
             HttpEntity entity = response.getEntity();
			 if(entity != null) {
				 try {
					 ESSerialThreadLocal.setESTypeReferences(types);

					 return super.converJson(entity, SearchHit.class);
//                 searchResponse = entity != null ? SimpleStringUtil.json2Object(entity.getContent(), SearchHit.class) : null;
//                 String content = EntityUtils.toString(entity);
//                 System.out.println(content);
//                 searchResponse = entity != null ? SimpleStringUtil.json2Object(content, RestResponse.class) : null;
				 } catch (Exception e) {
//                 logger.error("",e);
					 throw new ElasticSearchException(new StringBuilder().append("Request url:").append(url).toString(),e,status);
				 } finally {
					 ESSerialThreadLocal.clean();
				 }
			 }
//             ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(TransportClient.class);
//             NamedWriteableRegistry namedWriteableRegistry = (NamedWriteableRegistry)classInfo.getPropertyValue(clientUtil.getClient(),"namedWriteableRegistry");

             return null;

         } else {
             HttpEntity entity = response.getEntity();
//             if (entity != null ) {
//				throw new ElasticSearchException(EntityUtils.toString(entity),status);
//             }
//             else
//                 throw new ElasticSearchException("Unexpected response status: " + status,status);
			 return (SearchHit)super.handleException(url,entity,status);
         }
     }

}
