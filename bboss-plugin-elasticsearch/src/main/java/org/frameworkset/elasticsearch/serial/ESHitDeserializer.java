package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;

import java.io.IOException;

public class ESHitDeserializer extends JsonDeserializer implements ResolvableDeserializer {
	public ESHitDeserializer(){

	}
 
	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ESClass refs = ESSerialThreadLocal.getESTypeReferences();

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

	@Override
	public void resolve(DeserializationContext ctxt) throws JsonMappingException {
		

	}

}
