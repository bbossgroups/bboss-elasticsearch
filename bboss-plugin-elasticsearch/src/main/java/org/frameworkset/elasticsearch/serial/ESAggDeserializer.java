package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.SimpleType;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ESAggDeserializer extends JsonDeserializer implements ResolvableDeserializer {
	public ESAggDeserializer(){
		System.out.println();
	}

	private Class objectType;
	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String text = p.getText();
		while(true){
			JsonToken token =p.nextToken();
			if(token != null) {
				System.out.println(token + "," + p.getText());
				if(token.isStructEnd()){
					break;
				}
			}

			else
				break;
		}
		System.out.println(p.getCurrentToken());
		return null;
	}

	@Override
	public void resolve(DeserializationContext ctxt) throws JsonMappingException {
		CollectionType type = (CollectionType) ctxt.getContextualType();
		boolean y = type.hasGenericTypes();
		SimpleType t = (SimpleType) type.getContentType();
		JavaType rt = type.getReferencedType();
		Class<?> c = t.getRawClass();
		Type typt = c.getGenericSuperclass();
		if(typt instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) typt;
			Type[] params = parameterizedType.getActualTypeArguments();

			objectType = (Class) params[0];
		}
		else
			objectType = Object.class;


//		Map typs = GenericTypeResolver.getTypeVariableMap(c);
//		System.out.println(c);
	}

}
