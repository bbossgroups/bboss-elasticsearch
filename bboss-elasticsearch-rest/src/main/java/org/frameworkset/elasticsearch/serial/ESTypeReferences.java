package org.frameworkset.elasticsearch.serial;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ESTypeReferences<HIT,AGG> extends ESClass implements Comparable<ESTypeReferences<HIT,AGG>>{
//	private static String type2 = "com.fasterxml.jackson.core.type.TypeReference";
//	private static String type1 = "org.codehaus.jackson.type.TypeReference";
	 protected  Type hitType; 
	 protected  Type aggType;



	protected  Type innerHitType;



	public ESTypeReferences()
	{
		Type superClass = getClass().getGenericSuperclass();
//        if (superClass instanceof Class<?>) { // sanity check, should never happen
//            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
//        }
        /* 22-Dec-2008, tatu: Not sure if this case is safe -- I suspect
         *   it is possible to make it fail?
         *   But let's deal with specific
         *   case when we know an actual use case, and thereby suitable
         *   workarounds for valid case(s) and/or error to throw
         *   on invalid one(s).
         */
        if(superClass != null) {
			hitType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
			aggType = ((ParameterizedType) superClass).getActualTypeArguments()[1];
		}
		
	}
	 
	 

 
	  @Override
	public int compareTo(ESTypeReferences<HIT,AGG> o) { return 0; }




	public Type getHitType() {
		return hitType;
	}



 


	public Type getAggType() {
		return aggType;
	}




}
