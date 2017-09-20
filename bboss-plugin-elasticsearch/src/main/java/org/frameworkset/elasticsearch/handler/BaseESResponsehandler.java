package org.frameworkset.elasticsearch.handler;

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.entity.SearchResult;
import org.frameworkset.elasticsearch.serial.ESClass;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

public abstract class BaseESResponsehandler<B extends SearchResult> implements ResponseHandler<SearchResult> {
	protected ESClass types;

	public BaseESResponsehandler(ESTypeReferences types) {
		// TODO Auto-generated constructor stub
		this.types = types;
	}

	public BaseESResponsehandler(ESClassType types) {
		// TODO Auto-generated constructor stub
		this.types = types;
	}
	public BaseESResponsehandler(Class<?> types) {
		// TODO Auto-generated constructor stub
		this.types = new ESClassType(types);
	}
	public BaseESResponsehandler() {
		// TODO Auto-generated constructor stub
	}
	
	
	public ESClass getESTypeReferences(){
		return this.types;
	}

}
