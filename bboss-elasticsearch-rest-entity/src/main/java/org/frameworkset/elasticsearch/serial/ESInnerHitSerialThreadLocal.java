package org.frameworkset.elasticsearch.serial;

import java.util.HashMap;
import java.util.Map;

public abstract class ESInnerHitSerialThreadLocal {
	private static ThreadLocal<ESClass> innerHitTypeLocals = new ThreadLocal<ESClass>();
	private static ThreadLocal<Map<String,ESClass>> innerHitTypeLocalsByType = new ThreadLocal<Map<String,ESClass>>();
	public ESInnerHitSerialThreadLocal() {
		// TODO Auto-generated constructor stub
	}
	

	public static SerialContext buildSerialContext(){
		ESClass esClass = innerHitTypeLocals.get();
		Map<String,ESClass> innerHitTypes = innerHitTypeLocalsByType.get();
		if(esClass == null && innerHitTypes == null){
			return null;
		}
		SerialContext serialContext = new SerialContext(esClass,innerHitTypes);
		return serialContext;
	}

	public static void setESInnerTypeReferences(Class<?> refs){
		innerHitTypeLocals.set(new ESClassType(refs));
	}
	public static void setESInnerTypeReferences(ESClass refs){
		innerHitTypeLocals.set(refs);
	}

	/**
	 * 设置父子查询，子的类型信息和orm class对象
	 * @param type
	 * @param refs
	 */
	public static void setESInnerTypeReferences(String type,Class<?> refs){
		Map<String,ESClass> typeRefs = innerHitTypeLocalsByType.get();
		if(typeRefs == null) {
			typeRefs = new HashMap<String, ESClass>();
			innerHitTypeLocalsByType.set(typeRefs);
		}
		typeRefs.put(type,new ESClassType(refs));

	}
	/**
	 * 设置父子查询，子的类型信息和orm class对象
	 * @param typeRefs
	 */
	public static void setESInnerTypeReferences(Map<String,ESClass> typeRefs){
		typeRefs = new HashMap<String, ESClass>();
		innerHitTypeLocalsByType.set(typeRefs);
	}
	public static ESClass getESInnerTypeReferences(){
		return innerHitTypeLocals.get();
	}

	public static ESClass getESInnerTypeReferences(String type){
		Map<String,ESClass> typeRefs = innerHitTypeLocalsByType.get();
		if(typeRefs == null) {
			return innerHitTypeLocals.get();
		}
		ESClass esClass = typeRefs.get(type);
		return esClass;
	}
	public static void clean(){
		innerHitTypeLocals.set(null);
		innerHitTypeLocalsByType.set(null);
	}


}
