package org.frameworkset.elasticsearch.handler;

import com.fasterxml.jackson.databind.JavaType;
import org.frameworkset.elasticsearch.serial.ESClass;
import org.frameworkset.elasticsearch.serial.ESClassType;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;

import java.util.List;

public abstract class BaseResponsehandler extends BaseExceptionResponseHandler  {
	protected ESClass types;


	public BaseResponsehandler(ESTypeReferences types) {
		// TODO Auto-generated constructor stub
		this.types = types;
	}
    public BaseResponsehandler(JavaType javaType) {
        this.types = new ESClassType(javaType);
    }
	public BaseResponsehandler(ESClassType types) {
		// TODO Auto-generated constructor stub
		this.types = types;
	}
	public BaseResponsehandler(Class<?> types) {
		// TODO Auto-generated constructor stub
		this.types = new ESClassType(types);
	}

    public BaseResponsehandler(List<Class> mtypes) {
        // TODO Auto-generated constructor stub
        this.types = new ESClassType(mtypes);
    }
	public BaseResponsehandler() {
		// TODO Auto-generated constructor stub
	}
	
	
	public ESClass getESTypeReferences(){
		return this.types;
	}
}
