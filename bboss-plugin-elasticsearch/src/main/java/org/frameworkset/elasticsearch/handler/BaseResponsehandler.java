package org.frameworkset.elasticsearch.handler;

import org.frameworkset.elasticsearch.serial.ESClass;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

public abstract class BaseResponsehandler {
	protected ESClass types;

	public BaseResponsehandler(ESTypeReferences types) {
		// TODO Auto-generated constructor stub
		this.types = types;
	}

	public BaseResponsehandler(ESClassType types) {
		// TODO Auto-generated constructor stub
		this.types = types;
	}
	public BaseResponsehandler(Class<?> types) {
		// TODO Auto-generated constructor stub
		this.types = new ESClassType(types);
	}
	public BaseResponsehandler() {
		// TODO Auto-generated constructor stub
	}
	
	
	public ESClass getESTypeReferences(){
		return this.types;
	}
}
