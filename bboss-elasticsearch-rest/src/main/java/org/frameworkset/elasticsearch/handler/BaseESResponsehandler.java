package org.frameworkset.elasticsearch.handler;

import com.fasterxml.jackson.databind.JavaType;
import org.frameworkset.elasticsearch.entity.RestResponse;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.spi.remote.http.URLResponseHandler;

import java.util.List;

public abstract class BaseESResponsehandler extends BaseResponsehandler<RestResponse> implements URLResponseHandler<RestResponse> {


	public BaseESResponsehandler() {
		super();
		// TODO Auto-generated constructor stub
	}
    public BaseESResponsehandler(JavaType javaType) {
        super(javaType);
    }
	public BaseESResponsehandler(Class<?> types) {
		super(types);
		// TODO Auto-generated constructor stub
	}

    public BaseESResponsehandler(List<Class> types) {
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
