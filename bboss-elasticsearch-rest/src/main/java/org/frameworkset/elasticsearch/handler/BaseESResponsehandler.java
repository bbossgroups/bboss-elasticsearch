package org.frameworkset.elasticsearch.handler;

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.entity.RestResponse;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

public abstract class BaseESResponsehandler extends BaseResponsehandler implements ResponseHandler<RestResponse> {

	public BaseESResponsehandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BaseESResponsehandler(Class<?> types) {
		super(types);
		// TODO Auto-generated constructor stub
	}

	public BaseESResponsehandler(ESClassType types) {
		super(types);
		// TODO Auto-generated constructor stub
	}

	public BaseESResponsehandler(ESTypeReferences types) {
		super(types);
		// TODO Auto-generated constructor stub
	}
	

}
