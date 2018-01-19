package org.frameworkset.elasticsearch.serial;/*
 *  Copyright 2008 biaoping.yin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ESTypeReference<T> implements Comparable<ESTypeReference<T>>{

	protected  Type aggType;
	public ESTypeReference()
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
			aggType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
		}

	}




	@Override
	public int compareTo(ESTypeReference<T> o) { return 0; }









	public Type getAggType() {
		return aggType;
	}


}
