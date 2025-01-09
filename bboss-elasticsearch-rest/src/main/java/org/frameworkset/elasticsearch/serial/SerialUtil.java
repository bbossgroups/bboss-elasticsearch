package org.frameworkset.elasticsearch.serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.frameworkset.elasticsearch.ElasticsearchConstant;
import org.frameworkset.elasticsearch.entity.ESBaseData;
import org.frameworkset.elasticsearch.entity.ESId;
import org.frameworkset.json.Jackson2ObjectMapper;
import org.frameworkset.util.annotations.DateFormateMeta;

import java.io.Writer;

public class SerialUtil {
	protected static ObjectMapper normaMapper = null;
    protected static ObjectMapper disableCloseAndFlushMapper = null;
	protected static ObjectMapper esBaseDataFilterMapper = null;
	protected static ObjectMapper esIdFilterMapper = null;
	protected static DateFormateMeta dateFormateMeta;

    private static ClassLoader moduleClassLoader = SerialUtil.class.getClassLoader();

	static {
		dateFormateMeta = DateFormateMeta.buildDateFormateMeta("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",null,"Etc/UTC");

		init(dateFormateMeta);
	}
	public static DateFormateMeta getDateFormateMeta(){
		return dateFormateMeta;
	}

//
//
//    private static void registerWellKnownModulesIfAvailable(ObjectMapper objectMapper) {
//        // Java 7 java.nio.file.Path class present?
//        if (ClassUtils.isPresent("java.nio.file.Path", moduleClassLoader)) {
//            try {
//                Class<? extends Module> jdk7Module = (Class<? extends Module>)
//                        ClassUtils.forName("com.fasterxml.jackson.datatype.jdk7.Jdk7Module", moduleClassLoader);
//                objectMapper.registerModule(BeanUtils.instantiate(jdk7Module));
//            }
//            catch (ClassNotFoundException ex) {
//                // jackson-datatype-jdk7 not available
//            }
//        }
//
//        // Java 8 java.util.Optional class present?
//        if (ClassUtils.isPresent("java.util.Optional", moduleClassLoader)) {
//            try {
//                Class<? extends Module> jdk8Module = (Class<? extends Module>)
//                        ClassUtils.forName("com.fasterxml.jackson.datatype.jdk8.Jdk8Module", moduleClassLoader);
//                objectMapper.registerModule(BeanUtils.instantiate(jdk8Module));
//            }
//            catch (ClassNotFoundException ex) {
//                // jackson-datatype-jdk8 not available
//            }
//        }
//
//        // Java 8 java.time package present?
//        if (ClassUtils.isPresent("java.time.LocalDate", moduleClassLoader)) {
//            try {
//                Class<? extends Module> javaTimeModule = (Class<? extends Module>)
//                        ClassUtils.forName("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule", moduleClassLoader);
//                objectMapper.registerModule(BeanUtils.instantiate(javaTimeModule));
////               JavaTimeModule javaTimeModule = new JavaTimeModule();
////                LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"));
////                LocalDateTimeSerializer localDateTimeSerializer = new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'"));
////
////                javaTimeModule.addSerializer(LocalDateTime.class,localDateTimeSerializer);
////                javaTimeModule.addDeserializer(LocalDateTime.class,localDateTimeDeserializer);
////
////
////                objectMapper.registerModule(javaTimeModule);
//            }
//            catch (Exception ex) {
//                // jackson-datatype-jsr310 not available or older than 2.6
//                try {
//                    Class<? extends Module> jsr310Module = (Class<? extends Module>)
//                            ClassUtils.forName("com.fasterxml.jackson.datatype.jsr310.JSR310Module", moduleClassLoader);
//                    objectMapper.registerModule(BeanUtils.instantiate(jsr310Module));
//                }
//                catch (ClassNotFoundException ex2) {
//                    // OK, jackson-datatype-jsr310 not available at all...
//                }
//            }
//        }
//
//        // Joda-Time present?
//        if (ClassUtils.isPresent("org.joda.time.LocalDate", moduleClassLoader)) {
//            try {
//                Class<? extends Module> jodaModule = (Class<? extends Module>)
//                        ClassUtils.forName("com.fasterxml.jackson.datatype.joda.JodaModule", moduleClassLoader);
//                objectMapper.registerModule(BeanUtils.instantiate(jodaModule));
//            }
//            catch (ClassNotFoundException ex) {
//                // jackson-datatype-joda not available
//            }
//        }
//    }

    public static void init(DateFormateMeta dateFormateMeta){
		normaMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		normaMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, ElasticsearchConstant.FAIL_ON_UNKNOWN_PROPERTIES);
		normaMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		normaMapper.setDateFormat(dateFormateMeta.toDateFormat());
		normaMapper.setSerializerFactory(new DefaultEntityCustomSerializationFactory());

        Jackson2ObjectMapper.registerWellKnownModulesIfAvailable(normaMapper);

        disableCloseAndFlushMapper = new ObjectMapper();
        //反序列化时，属性不存在时忽略属性
        disableCloseAndFlushMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, ElasticsearchConstant.FAIL_ON_UNKNOWN_PROPERTIES);
        disableCloseAndFlushMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        disableCloseAndFlushMapper.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);
        disableCloseAndFlushMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        disableCloseAndFlushMapper.setDateFormat(dateFormateMeta.toDateFormat());
        disableCloseAndFlushMapper.setSerializerFactory(new DefaultEntityCustomSerializationFactory());


        Jackson2ObjectMapper.registerWellKnownModulesIfAvailable(disableCloseAndFlushMapper);
        
		esBaseDataFilterMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		esBaseDataFilterMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, ElasticsearchConstant.FAIL_ON_UNKNOWN_PROPERTIES);
		esBaseDataFilterMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		esBaseDataFilterMapper.setDateFormat(dateFormateMeta.toDateFormat());
		esBaseDataFilterMapper.setSerializerFactory(new ESBaseDataEntityCustomSerializationFactory());
        Jackson2ObjectMapper.registerWellKnownModulesIfAvailable(esBaseDataFilterMapper);
		esIdFilterMapper = new ObjectMapper();
		//反序列化时，属性不存在时忽略属性
		esIdFilterMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, ElasticsearchConstant.FAIL_ON_UNKNOWN_PROPERTIES);
		esIdFilterMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		esIdFilterMapper.setDateFormat(dateFormateMeta.toDateFormat());
		esIdFilterMapper.setSerializerFactory(new ESIdEntityCustomSerializationFactory());
        Jackson2ObjectMapper.registerWellKnownModulesIfAvailable(esIdFilterMapper);
        
        
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
			throw new IllegalArgumentException("Error JSON serialization operation",e);
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
			throw new IllegalArgumentException("Error JSON serialization operation",e);
		}




	}
    public  static void object2jsonDisableCloseAndFlush(Object bean, Writer writer) {
        try {

            disableCloseAndFlushMapper.writeValue(writer,bean);

        } catch (Exception e) {
            throw new IllegalArgumentException("Error JSON serialization operation",e);
        }
    }
	public  static void normalObject2json(Object bean, Writer writer) {
		try {

			normaMapper.writeValue(writer,bean);

		} catch (Exception e) {
			throw new IllegalArgumentException("Error JSON serialization operation",e);
		}
	}
}
