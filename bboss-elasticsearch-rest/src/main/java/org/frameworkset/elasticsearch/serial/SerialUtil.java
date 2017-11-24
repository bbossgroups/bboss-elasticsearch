package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.frameworkset.elasticsearch.entity.ESBaseData;
import org.frameworkset.elasticsearch.entity.ESId;

import java.io.Writer;

public class SerialUtil {
	protected static ObjectMapper normaMapper = null;
	protected static ObjectMapper esBaseDataFilterMapper = null;
	protected static ObjectMapper esIdFilterMapper = null;
	static {
		normaMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		normaMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		esBaseDataFilterMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		esBaseDataFilterMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		esBaseDataFilterMapper.setSerializerFactory(new ESBaseDataEntityCustomSerializationFactory());
		esIdFilterMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		esIdFilterMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		esIdFilterMapper.setSerializerFactory(new ESIdEntityCustomSerializationFactory());
	}
	public static String object2json(Object bean){
		try {
			Class<?> beanClass = bean.getClass();
			if(ESBaseData.class.isAssignableFrom(beanClass) ) {
				String value = esBaseDataFilterMapper.writeValueAsString(bean);
				return value;
			}
			else if(ESId.class.isAssignableFrom(beanClass) ) {
				String value = esIdFilterMapper.writeValueAsString(bean);
				return value;
			}
			else{
				String value = normaMapper.writeValueAsString(bean);
				return value;
			}



		} catch (Exception e) {
			throw new IllegalArgumentException("错误的json序列化操作",e);
		}
	}

	public  static void object2json(Object bean, Writer writer) {
		try {
			Class<?> beanClass = bean.getClass();
			if(ESBaseData.class.isAssignableFrom(beanClass) ) {
				 esBaseDataFilterMapper.writeValue(writer,bean);

			}
			else if(ESId.class.isAssignableFrom(beanClass) ) {
				 esIdFilterMapper.writeValue(writer,bean);
			}
			else{
				 normaMapper.writeValue(writer,bean);
			}
		} catch (Exception e) {
			throw new IllegalArgumentException("错误的json序列化操作",e);
		}




	}
}
