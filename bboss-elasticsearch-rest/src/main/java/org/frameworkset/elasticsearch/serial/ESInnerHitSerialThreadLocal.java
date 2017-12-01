package org.frameworkset.elasticsearch.serial;

public abstract class ESInnerHitSerialThreadLocal {
	private static ThreadLocal<ESClass> innerHitTypeLocals = new ThreadLocal<ESClass>();
	public ESInnerHitSerialThreadLocal() {
		// TODO Auto-generated constructor stub
	}
	



	public static void setESInnerTypeReferences(Class<?> refs){
		innerHitTypeLocals.set(new ESClassType(refs));
	}
	public static ESClass getESInnerTypeReferences(){
		return innerHitTypeLocals.get();
	}
	public static void clean(){
		innerHitTypeLocals.set(null);
	}


}
