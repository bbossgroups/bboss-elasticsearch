package org.frameworkset.elasticsearch.handler;

import org.frameworkset.elasticsearch.entity.SearchHit;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.spi.remote.http.URLResponseHandler;

public abstract class BaseGetDocESResponsehandler  extends BaseResponsehandler  implements URLResponseHandler<SearchHit> {

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
