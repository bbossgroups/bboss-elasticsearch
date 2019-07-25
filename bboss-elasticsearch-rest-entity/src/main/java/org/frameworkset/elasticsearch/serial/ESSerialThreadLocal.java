package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;
import java.util.Map;

public abstract class ESSerialThreadLocal {
	private static ThreadLocal<ESClass> typeLocals = new ThreadLocal<ESClass>();
	public ESSerialThreadLocal() {
		// TODO Auto-generated constructor stub
	}
	
	public static void setESTypeReferences(ESClass refs){
		if(typeLocals.get() == null)
			typeLocals.set(refs);
	}

	public static void setESTypeReferences(Class<?> refs){
		typeLocals.set(new ESClassType(refs));
	}
	public static ESClass getESTypeReferences(){
		return typeLocals.get();
	}



	public static void clean(){
		typeLocals.set(null);
	}
	private static JavaType mapObjectType;
	public static JavaType getMapObjectType(){
		if(  mapObjectType != null)
			return mapObjectType;
		mapObjectType = TypeFactory.defaultInstance().constructType(new TypeReference<Map<String,Object>>(){});
		return mapObjectType;
	}

	public static JavaType getJavaType(Type type){
		return TypeFactory.defaultInstance().constructType(type);
	}

}
