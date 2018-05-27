package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.frameworkset.elasticsearch.entity.ESBaseData;
import org.frameworkset.elasticsearch.entity.ESId;
import org.frameworkset.util.annotations.DateFormateMeta;

import java.io.Writer;

public class SerialUtil {
	protected static ObjectMapper normaMapper = null;
	protected static ObjectMapper esBaseDataFilterMapper = null;
	protected static ObjectMapper esIdFilterMapper = null;
	protected static DateFormateMeta dateFormateMeta;
	static {
		dateFormateMeta = DateFormateMeta.buildDateFormateMeta("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"Etc/UTC");

		init(dateFormateMeta);
	}
	public static DateFormateMeta getDateFormateMeta(){
		return dateFormateMeta;
	}
	public static void init(DateFormateMeta dateFormateMeta){
		normaMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		normaMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		normaMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		normaMapper.setDateFormat(dateFormateMeta.toDateFormat());
		normaMapper.setSerializerFactory(new DefaultEntityCustomSerializationFactory());
		esBaseDataFilterMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		esBaseDataFilterMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		esBaseDataFilterMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		esBaseDataFilterMapper.setDateFormat(dateFormateMeta.toDateFormat());
		esBaseDataFilterMapper.setSerializerFactory(new ESBaseDataEntityCustomSerializationFactory());

		esIdFilterMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		esIdFilterMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		esIdFilterMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		esIdFilterMapper.setDateFormat(dateFormateMeta.toDateFormat());
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
