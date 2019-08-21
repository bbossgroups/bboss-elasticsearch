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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.frameworkset.elasticsearch.entity.InnerSearchHit;

import java.io.IOException;

public class ESInnerHitDeserializer  extends BaseESHitDeserializer {
	public ESInnerHitDeserializer(){

	}

	@Override
	protected ESClass getESInnerTypeReferences() {
		return ESInnerHitSerialThreadLocal.getESInnerTypeReferences();
	}

	protected ESClass getESInnerTypeReferences(String type) {
		return ESInnerHitSerialThreadLocal.getESInnerTypeReferences(type);
	}
	private String getTypeName(JsonParser p){
		return p.getParsingContext().getParent().getParent().getParent().getParent().getParent().getCurrentName();
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonStreamContext jsonStreamContext = p.getParsingContext().getParent();
		InnerSearchHit hit = (InnerSearchHit) jsonStreamContext.getCurrentValue();
		ESClass refs = null;
		if(hit != null) {
			if (hit.getIndex() == null) {
				refs = getESInnerTypeReferences(hit.getType());
			} else {
				refs = getESInnerTypeReferences(getTypeName(p));
			}
		}
		if(refs == null){
			return ctxt.findRootValueDeserializer(ESSerialThreadLocal.getMapObjectType()).deserialize(p,ctxt);
		}
		else
		{
			if(refs instanceof ESTypeReferences)
				return ctxt.findRootValueDeserializer(ESSerialThreadLocal.getJavaType(((ESTypeReferences)refs).getHitType())).deserialize(p,ctxt);
			else
			{
				ESClassType classType = (ESClassType)refs;
				return ctxt.findRootValueDeserializer(ESSerialThreadLocal.getJavaType(classType.getHitClass())).deserialize(p,ctxt);

			}
		}


	}


}
