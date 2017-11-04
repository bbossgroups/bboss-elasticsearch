package org.frameworkset.elasticsearch.handler;

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.entity.SearchHit;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

public abstract class BaseGetDocESResponsehandler  extends BaseResponsehandler  implements ResponseHandler<SearchHit> {

	public BaseGetDocESResponsehandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BaseGetDocESResponsehandler(Class<?> types) {
		super(types);
		// TODO Auto-generated constructor stub
	}

	public BaseGetDocESResponsehandler(ESClassType types) {
		super(types);
		// TODO Auto-generated constructor stub
	}

	public BaseGetDocESResponsehandler(ESTypeReferences types) {
		super(types);
		// TODO Auto-generated constructor stub
	}
	 

}
