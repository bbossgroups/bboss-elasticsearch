/*
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

package org.frameworkset.elasticsearch.template;

import bboss.org.apache.velocity.VelocityContext;
import com.frameworkset.util.*;
import com.frameworkset.velocity.BBossVelocityUtil;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.ElasticsearchParseException;
import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.elasticsearch.serial.CustomCharEscapeUtil;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.Param;
import org.frameworkset.util.ClassUtil;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.frameworkset.util.annotations.wraper.ColumnWraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.*;

/**
 * <p>
 * Title: SQLUtil.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * bboss workgroup
 * </p>
 * <p>
 * Copyright (c) 2007
 * </p>
 * 
 * @Date 2010-7-12 下午07:38:55
 * @author biaoping.yin
 * @version 1.0
 */
public class ESUtil {
	protected TemplateContainer templatecontext;
	private static Logger log = LoggerFactory.getLogger(ESUtil.class);
	protected static Map<String,ESUtil> esutils = new HashMap<String,ESUtil>(); 
	protected static long refresh_interval = 5000;
	public static final int defaultPerKeyDSLStructionCacheSize = 2000;
	public static final boolean defaultAlwaysCacheDslStruction = false;
	protected int perKeyDSLStructionCacheSize;
	protected boolean alwaysCacheDslStruction;
	protected  ESTemplateCache templateCache ;
	protected Map<String,ESInfo> esInfos;
	protected Map<String,ESRef> esrefs;
	protected boolean hasrefs;
	protected String templateFile;
	protected String dslMappingDir;
	protected String realTemplateFile;
	protected boolean destroyed = false;
	public VariableHandler.URLStruction getTempateStruction(ESInfo esInfo, String template) {
		return this.templateCache.getTemplateStruction(esInfo,template);
	}

	/**
	 * since 5.0.6.0,去掉vm模板解析时变量的转义，因此在模板中的使用$aaa模式变量的情况时，需要注意转义的问题，如果存在转义问题，请使用#[]模式变量
	 * 日期也不做格式化转换，使用$aaa模式变量的情况下需要注意日期格式问题，如果存在日期，则需要使用#[]模式变量，这样bboss会根据bean属性配置的格式，或者变量中指定的格式
	 * 对日期进行格式化，如果不指定格式采用默认的格式和时区进行处理
	 * @param bean
	 * @return
	 */
	public VelocityContext buildVelocityContext(Object bean) {
		VelocityContext context_ = new VelocityContext();
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		String name = null;
		DateFormateMeta dataformat = null;

//		String charset = null;
		Object value = null;
//		Class type = null;

//		Method writeMethod = null;

		List<ClassUtil.PropertieDescription> attributes = beanInfo.getPropertyDescriptors();
		for (int i = 0; attributes != null && i < attributes.size(); i++) {
			ClassUtil.PropertieDescription property = attributes.get(i);
			ColumnWraper column = property.getColumn();
			if (column != null && (column.ignoreCUDbind() || column.ignorebind()))
				continue;

//			type = property.getPropertyType();


			try {
				if (property.canread()) {
					try {
						value = property.getValue(bean);
					} catch (InvocationTargetException e1) {
						log.error("Failed to get attribute[" + beanInfo.getClazz().getName() + "." + property.getName() + "] value:", e1.getTargetException());
					} catch (Exception e1) {
						log.error("Failed to get attribute[" + beanInfo.getClazz().getName() + "." + property.getName() + "] value:", e1);
					}

					name = property.getName();

					if (column != null) {
						ColumnEditorInf editor = column.editor();
						if (editor == null || editor instanceof ColumnToFieldEditor) {

							dataformat = column.getDateFormateMeta();

//							charset = column.charset();


						} else {
							Object cv = editor.toColumnValue(column, value);
							if (cv == null)
								throw new ElasticSearchException("转换属性[" + beanInfo.getClazz().getName() + "." + property.getName() + "]值失败：值为null时，转换器必须返回ColumnType类型的对象,用来指示表字段对应的java类型。");

							if (!(cv instanceof ColumnType)) {
								value = cv;
//								type = value.getClass();

							} else {
//								type = ((ColumnType) cv).getType();
							}
						}

					}
					/**
					 * since 5.0.6.0,去掉vm模板解析时变量的转义，因此在模板中的使用$aaa模式变量的情况时，需要注意转义的问题，如果存在转义问题，请使用#[]模式变量
					 * 	 日期也不做格式化转换，使用$aaa模式变量的情况下需要注意日期格式问题，如果存在日期，则需要使用#[]模式变量，这样bboss会根据bean属性配置的格式，或者变量中指定的格式
					 * 	 对日期进行格式化，如果不指定格式采用默认的格式和时区进行处理
					 */
					/**
					if (value == null) {
						context_.put(name, null);
					} else if (value instanceof Date) {
//						if(dataformat != null)
							context_.put(name, this.getDate((Date) value, dataformat));
//						else {
////							context_.put(name, ((Date) value).getTime());
//							context_.put(name, ((Date) value));
//						}
					} else if(value instanceof String){//提前转义
						CharEscapeUtil charEscapeUtil = new CharEscapeUtil();
						charEscapeUtil.writeString((String)value,false);
						context_.put(name, charEscapeUtil.toString());
					}
					else {
						context_.put(name, value);
					}
					 */
					context_.put(name, value);
//					params.addSQLParamWithDateFormateMeta(name, value, sqltype, dataformat,charset);

				}
				name = null;
				value = null;
				dataformat = null;

//				charset = null;


			} catch (SecurityException e) {
				throw new ElasticSearchException(e);
			} catch (IllegalArgumentException e) {
				throw new ElasticSearchException(e);
			}
//			catch (InvocationTargetException e) {
//				throw new ElasticSearchException(e.getTargetException());
//			}


			catch (Exception e) {
				throw new ElasticSearchException(e);
			}


		}


		return context_;

	}


	/**
	 * since 5.0.6.0,去掉vm模板解析时变量的转义，因此在模板中的使用$aaa模式变量的情况时，需要注意转义的问题，如果存在转义问题，请使用#[]模式变量
	 *
	 * @param data
	 * @return
	 */
	public VelocityContext buildVelocityContext(Map data) {

		VelocityContext context_ = new VelocityContext();
		Iterator<Map.Entry<String, Param>> it = data.entrySet().iterator();
		Object temp = null;
		while (it.hasNext()) {
			Map.Entry<String, Param> entry = it.next();
			temp = entry.getValue();
			/**
			if (temp != null){
				if(temp instanceof String) {
					CharEscapeUtil charEscapeUtil = new CharEscapeUtil();
					charEscapeUtil.writeString((String) temp,false);
					temp = charEscapeUtil.toString();
				}
				context_.put(entry.getKey(), temp);
			}*/
			context_.put(entry.getKey(), temp);
		}

		return context_;

	}

	public VelocityContext buildVelocityContext() {

		VelocityContext context_ = new VelocityContext();

		return context_;

	}
	public static String getDate(Date date){
		return getDate(date, (DateFormateMeta )null);
	}
	public static String getDate(Date date, DateFormateMeta dateFormateMeta){
		String format = null;
		DateFormat f = null;
		if(dateFormateMeta == null ) {
//			format = this.getJavaDateFormat();
//			dateFormateMeta = DateFormateMeta.buildDateFormateMeta(format);
			dateFormateMeta = SerialUtil.getDateFormateMeta();
		}
		f = dateFormateMeta.toDateFormat();
		try {
			String _date = f.format(date);
			return _date;
		}
		catch (Exception e) {
			throw new ElasticSearchException(e);
		}
	}

	public static String getDate(Date date, DateFormat f){

		try {
			String _date = f.format(date);
			return _date;
		}
		catch (Exception e) {
			throw new ElasticSearchException(e);
		}
	}
	public void handleVaribleValue(StringBuilder builder,ESTemplateCache.TempateVariable variable,String value,boolean escape){

		int escapeCount = variable.getEscapeCount();
		if(variable.isQuoted()) {
			if(escapeCount <= 1)
				builder.append("\"");
			else{
				for(int i = 0; i < escapeCount-1; i ++){
					builder.append("\\");
				}
				builder.append("\"");
			}

		}
		if(variable.getLpad() != null || variable.getRpad() != null) {
			StringBuilder innerValue = new StringBuilder();
			if (variable.getLpad() != null) {
				innerValue.append(variable.getLpad());
			}
			innerValue.append(value);

			if (variable.getRpad() != null) {
				innerValue.append(variable.getRpad());
			}
			if (!escape) {
				builder.append(innerValue.toString());
			} else {
				if(escapeCount <= 1){
					CharEscapeUtil charEscapeUtil = new CustomCharEscapeUtil(new BBossStringWriter(builder),variable.getEsEncode());
					charEscapeUtil.writeString(innerValue.toString(), true);
				}
				else{
					String innerValueString = innerValue.toString();
					innerValue.setLength(0);
					for(int i = 0; i < escapeCount; i ++) {
						CharEscapeUtil charEscapeUtil = new CustomCharEscapeUtil(new BBossStringWriter(innerValue),variable.getEsEncode());
						charEscapeUtil.writeString(innerValueString, true);
						innerValueString = innerValue.toString();
						innerValue.setLength(0);
					}
					builder.append(innerValueString);
				}

			}
		}
		else{
			if (!escape) {
				builder.append(value);
			} else {

//				CharEscapeUtil charEscapeUtil = new CharEscapeUtil(new BBossStringWriter(builder));
//				charEscapeUtil.writeString(value, true);

				if(escapeCount <= 1){
					CharEscapeUtil charEscapeUtil = new CustomCharEscapeUtil(new BBossStringWriter(builder),variable.getEsEncode());
					charEscapeUtil.writeString(value, true);
				}
				else{
					StringBuilder innerValue = new StringBuilder();
					for(int i = 0; i < escapeCount; i ++) {
						CharEscapeUtil charEscapeUtil = new CustomCharEscapeUtil(new BBossStringWriter(innerValue),variable.getEsEncode());
						charEscapeUtil.writeString(value, true);
						value = innerValue.toString();
						innerValue.setLength(0);
					}
					builder.append(value);
				}
			}
		}
		if(variable.isQuoted()) {
//			builder.append("\"");
			if(escapeCount <= 1)
				builder.append("\"");
			else{
				for(int i = 0; i < escapeCount-1; i ++){
					builder.append("\\");
				}
				builder.append("\"");
			}
		}


	}
	public void getVariableValue(StringBuilder builder, ESTemplateCache.TempateVariable variable, Object bean, List<ClassUtil.PropertieDescription> attributes, ClassUtil.ClassInfo beanInfo, String template) {
//		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		String name = null;
		DateFormateMeta dataformat = variable.getDateFormateMeta();//优先采用模板变量参数中的日期格式
		Boolean escape = variable.getEscape();
//		String charset = null;
		Object value = null;
		Class type = null;

//		Method writeMethod = null;

//		List<ClassUtil.PropertieDescription> attributes = beanInfo.getPropertyDescriptors();
		for (int i = 0; attributes != null && i < attributes.size(); i++) {
			ClassUtil.PropertieDescription property = attributes.get(i);
			if(!property.getName().equals(variable.getVariableName()))
				continue;
			ColumnWraper column = property.getColumn();
			if (column != null && (column.ignoreCUDbind() || column.ignorebind()))
				continue;

			type = property.getPropertyType();

			try {
				if (property.canread()) {
					try {
						value = property.getValue(bean);
					} catch (InvocationTargetException e1) {
						log.error(new StringBuilder().append("Failed to get attribute[" ).append( beanInfo.getClazz().getName() ).append( "." + property.getName() ).append( "] value:Check the template definition").append("[")
								.append(template).append("]@").append(this.templatecontext.getNamespace()).toString(), e1.getTargetException());
					} catch (Exception e1) {
						log.error(new StringBuilder().append("Failed to get attribute[" ).append( beanInfo.getClazz().getName() ).append( "." + property.getName() ).append( "] value:Check the template definition").append("[")
								.append(template).append("]@").append(this.templatecontext.getNamespace()).toString(), e1);
					}

					name = property.getName();

					if (column != null) {
						if(escape == null){
							escape = column.isEscape();
						}
						ColumnEditorInf editor = column.editor();
						if (editor == null || editor instanceof ColumnToFieldEditor) {
							if(dataformat == null)
								dataformat = column.getDateFormateMeta();

//							charset = column.charset();


						} else {
							Object cv = editor.toColumnValue(column, value);
							if (cv == null)
								throw new ElasticSearchException(new StringBuilder().append("Transform property[" )
										.append( beanInfo.getClazz().getName() ).append( "." )
										.append( property.getName() )
										.append( "] value failed: When the value is null, the converter must return an object of ColumnType type to indicate the Java type corresponding to the table field. Check the template definition")
										.append("[")
      										.append(template).append("]@")
										.append(this.templatecontext.getNamespace()).toString());

							if (!(cv instanceof ColumnType)) {
								value = cv;
								type = value.getClass();

							} else {
								type = ((ColumnType) cv).getType();
							}
						}

					}
					if (value == null) {
						builder.append("null");
					} else if (value instanceof Date) {
						String value_ = this.getDate((Date) value, dataformat);
						handleVaribleValue(  builder,  variable,value_,false);

					} else {
						value = VariableHandler.evaluateVariableValue(variable, value);
						if(value == null){
							builder.append("null");
						}
						else if(value instanceof String){
							if(escape == null)
								handleVaribleValue(  builder,  variable,(String)value,true);
							else
								handleVaribleValue(  builder,  variable,(String)value,escape.booleanValue());//默认对字符串进行转义处理

						}
						else if (value instanceof Date) {
							String value_ = this.getDate((Date) value, dataformat);
							handleVaribleValue(  builder,  variable,value_,false);

						}
						else {
//							if(variable.getLpad() != null){
//								builder.append(variable.getLpad());
//							}
//							builder.append(value.toString());
//							if(variable.getRpad() != null){
//								builder.append(variable.getRpad());
//							}
							handleObject(variable,builder,value);
						}
					}
//					params.addSQLParamWithDateFormateMeta(name, value, sqltype, dataformat,charset);

				}
				name = null;
				value = null;
				dataformat = null;

				return;

			} catch (SecurityException e) {
				throw new ElasticSearchException(new StringBuilder().append("Failed to convert attribute values: Check template definitions").append("[")
						.append(template).append("]@").append(this.templatecontext.getNamespace()).toString(),e);
			} catch (IllegalArgumentException e) {
				throw new ElasticSearchException(new StringBuilder().append("Failed to convert attribute values: Check template definitions").append("[")
						.append(template).append("]@").append(this.templatecontext.getNamespace()).toString(),e);
			}
//			catch (InvocationTargetException e) {
//				throw new ElasticSearchException(e.getTargetException());
//			}


			catch (Exception e) {
				throw new ElasticSearchException(e);
			}


		}
		throw new ElasticsearchParseException(new StringBuilder().append(beanInfo.getClazz().getName()).append("No value are specified for variable").append("[").append(variable.getVariableName()).append("] of the elasticsearch dsl template[")
				.append(template).append("]@").append(this.templatecontext.getNamespace())
				.toString());





	}

	public void evalStruction(StringBuilder builder,VariableHandler.URLStruction templateStruction,Object bean,String template){
		List<String> tokens = templateStruction.getTokens();
		List<VariableHandler.Variable> variables = templateStruction.getVariables();
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		List<ClassUtil.PropertieDescription> attributes = beanInfo.getPropertyDescriptors();
		for(int i = 0; i < tokens.size(); i ++){
			builder.append(tokens.get(i));
			if(i < variables.size()) {
				ESTemplateCache.TempateVariable variable = (ESTemplateCache.TempateVariable)variables.get(i);
				this.getVariableValue(builder, variable, bean, attributes, beanInfo, template);
			}

		}
	}

	public void evalStruction(StringBuilder builder,VariableHandler.URLStruction templateStruction,Map bean,String template){
		List<String> tokens = templateStruction.getTokens();
		List<VariableHandler.Variable> variables = templateStruction.getVariables();
		for(int i = 0; i < tokens.size(); i ++){
			builder.append(tokens.get(i));
			if(i < variables.size()) {
				ESTemplateCache.TempateVariable variable = (ESTemplateCache.TempateVariable)variables.get(i);
				Boolean escape = variable.getEscape();
				Object data = bean.get(variable.getVariableName());
				if (data == null) {
					if (bean.containsKey(variable.getVariableName()))
						builder.append("null");
					else {
						throw new ElasticsearchParseException(new StringBuilder()
								.append("No value are specified for variable")
								.append("[")
								.append(variable.getVariableName()).append("] of the elasticsearch dsl template[")
								.append(template).append("]@").append(this.templatecontext.getNamespace())
								.append(" Error dsl:\r\n")
								.append(builder.toString())
								.append("\r\n Dsl from config file:\r\n")
								.append(templateStruction.getUrl())
								.toString());
					}
				} else {
					Object value = data;//bean.get(variable.getVariableName());
					if(value instanceof Date){
						String value_ = this.getDate((Date) value, variable.getDateFormateMeta());
						this.handleVaribleValue(builder,variable,value_,false);
					}
					else {

						value = VariableHandler.evaluateVariableValue(variable, value);
						if(value == null){
							builder.append("null");
						}
						else if(value instanceof Date){
							String value_ = this.getDate((Date) value, variable.getDateFormateMeta());
							this.handleVaribleValue(builder,variable,value_,false);
						}
						else if (value instanceof String) {//如果值没有变化，则是否转义由escapeValue参数决定
							if(escape == null) {
								this.handleVaribleValue(builder, variable, (String) value, true);
							}
							else{
								this.handleVaribleValue(builder, variable, (String) value, escape.booleanValue());
							}
						} else {
//							if(variable.getSerialJson() == null) {
//								if (variable.getLpad() != null) {
//									builder.append(variable.getLpad());
//								}
//								builder.append(value.toString());
//								if (variable.getRpad() != null) {
//									builder.append(variable.getRpad());
//								}
//							}
							handleObject(variable,builder,value);
						}
					}
				}
			}

		}

	}
	private  void handleObject(ESTemplateCache.TempateVariable variable,StringBuilder builder,Object value){
		if(variable.getSerialJson() == null
				|| variable.getSerialJson().booleanValue() == false) {
			if (variable.getLpad() != null) {
				builder.append(variable.getLpad());
			}
			builder.append(value.toString());
			if (variable.getRpad() != null) {
				builder.append(variable.getRpad());
			}
		}
		else{
			int escapeCount = variable.getEscapeCount();
			if(escapeCount <= 1) {
				builder.append(SerialUtil.object2json(value));
			}
			else{
				String _value = SerialUtil.object2json(value);
				StringBuilder innerValue = new StringBuilder();
				for(int i = 0; i < escapeCount - 1; i ++) {
					CharEscapeUtil charEscapeUtil = new CustomCharEscapeUtil(new BBossStringWriter(innerValue),variable.getEsEncode());
					charEscapeUtil.writeString(_value, true);
					_value = innerValue.toString();
					innerValue.setLength(0);
				}
				builder.append(_value);
			}
		}
	}

	public static class ESRef
	{
		private String dslMappingDir;
		public ESRef(String esname, String templatefile, String name,String dslMappingDir) {
			super();
			this.esname = esname;
			this.templatefile = templatefile;
			this.name = name;
			this. dslMappingDir = dslMappingDir;
		}
		private ESUtil esutil;
		private String esname;
		private String templatefile;
		private String name;
		public String getESname() {
			return esname;
		}
		public String getTemplatefile() {
			return templatefile;
		}
		public String getName() {
			return name;
		}
		public ESInfo getESInfo()
		{
			if(esutil == null)
			{
				init();
			}
			return this.esutil.getESInfo(esname);
		}
		private synchronized void init()
		{
			if(esutil == null)
			{
				this.esutil = ESUtil.getInstance(dslMappingDir,templatefile);
			}
		}
		public String getTemplate() {
			if(esutil == null)
			{
				init();
			}
			return this.esutil.getTemplate(esname);
		}
		public String getTemplate( Map variablevalues) {
			if(esutil == null)
			{
				init();
			}
			return this.esutil.getTemplate( esname, variablevalues);
		}
		
	}
	
	public Map<String,ESRef> getESRefers()
	{
		return this.esrefs;
	}
	 
//	/**
//	 * sql语句velocity模板索引表，以sql语句的名称为索引
//	 * 当sql文件重新加载时，这些模板也会被重置
//	 */
//	private Map<String,SQLTemplate> sqlVelocityTemplates;
//	
	
	static DaemonThread damon = null;

	/**
	 * Returns a string whose value is this string, with any leading and trailing
	 * whitespace removed.
	 * <p>
	 * If this {@code String} object represents an empty character
	 * sequence, or the first and last characters of character sequence
	 * represented by this {@code String} object both have codes
	 * greater than {@code '\u005Cu0020'} (the space character), then a
	 * reference to this {@code String} object is returned.
	 * <p>
	 * Otherwise, if there is no character with a code greater than
	 * {@code '\u005Cu0020'} in the string, then a
	 * {@code String} object representing an empty string is
	 * returned.
	 * <p>
	 * Otherwise, let <i>k</i> be the index of the first character in the
	 * string whose code is greater than {@code '\u005Cu0020'}, and let
	 * <i>m</i> be the index of the last character in the string whose code
	 * is greater than {@code '\u005Cu0020'}. A {@code String}
	 * object is returned, representing the substring of this string that
	 * begins with the character at index <i>k</i> and ends with the
	 * character at index <i>m</i>-that is, the result of
	 * {@code this.substring(k, m + 1)}.
	 * <p>
	 * This method may be used to trim whitespace (as defined above) from
	 * the beginning and end of a string.
	 *
	 * @return  A string whose value is this string, with any leading and trailing white
	 *          space removed, or this string if it has no leading or
	 *          trailing white space.
	 */
	public static String ltrim(String value) {

		int len = value.length();
		int st = 0;


		while ((st < len) && (value.charAt(st) <= ' ')) {
			st++;
		}

		return (st > 0)  ? value.substring(st,len) : value;
	}

	/**
	 * Returns a string whose value is this string, with any leading and trailing
	 * whitespace removed.
	 * <p>
	 * If this {@code String} object represents an empty character
	 * sequence, or the first and last characters of character sequence
	 * represented by this {@code String} object both have codes
	 * greater than {@code '\u005Cu0020'} (the space character), then a
	 * reference to this {@code String} object is returned.
	 * <p>
	 * Otherwise, if there is no character with a code greater than
	 * {@code '\u005Cu0020'} in the string, then a
	 * {@code String} object representing an empty string is
	 * returned.
	 * <p>
	 * Otherwise, let <i>k</i> be the index of the first character in the
	 * string whose code is greater than {@code '\u005Cu0020'}, and let
	 * <i>m</i> be the index of the last character in the string whose code
	 * is greater than {@code '\u005Cu0020'}. A {@code String}
	 * object is returned, representing the substring of this string that
	 * begins with the character at index <i>k</i> and ends with the
	 * character at index <i>m</i>-that is, the result of
	 * {@code this.substring(k, m + 1)}.
	 * <p>
	 * This method may be used to trim whitespace (as defined above) from
	 * the beginning and end of a string.
	 *
	 * @return  A string whose value is this string, with any leading and trailing white
	 *          space removed, or this string if it has no leading or
	 *          trailing white space.
	 */
	public static String rtrim(String value) {

		int len = value.length();
		int st = 0;


		while ((st < len) && (value.charAt(len - 1) <= ' ')) {
			len--;
		}
		return ((len < value.length())) ? value.substring(st, len) : value;
	}


	/**
	 * 
	 */
	private void trimValues()
	{
		if(this.templatecontext == null)
			return;
//		this.esInfos= null;
//		this.esrefs = null;
		Map<String,ESInfo> esInfos = new HashMap<String,ESInfo>();
		Map<String,ESRef> esrefs = new HashMap<String,ESRef> ();
		Set keys = this.templatecontext.getTempalteNames();
		if(keys != null && keys.size() > 0)
		{
			Iterator<String> keys_it = keys.iterator();
			while(keys_it.hasNext())
			{
				String key = keys_it.next();
				TemplateMeta pro = this.templatecontext.getProBean(key);
				String templateFile = pro.getReferenceNamespace();//pro.getExtendAttribute("templateFile");
				if(templateFile == null)
				{
					Object o = pro.getDslTemplate();
					if(o instanceof String)
					{
						
						String value = (String)o;
						
						if(value != null)
						{
							boolean cache = pro.getCache() != null? pro.getCache():true;
							boolean istpl = pro.getVtpl() != null? pro.getVtpl():true;//pro.getBooleanExtendAttribute("istpl",true);//标识sql语句是否为velocity模板
							boolean multiparser = pro.getMultiparser() != null? pro.getMultiparser():istpl;//pro.getBooleanExtendAttribute("multiparser",istpl);//如果sql语句为velocity模板，则在批处理时是否需要每条记录都需要分析sql语句
							ESTemplate sqltpl = null;
							value = ESUtil.ltrim(value);
							ESInfo sqlinfo = new ESInfo(key, value, istpl,multiparser,pro, cache);
							sqlinfo.setEsUtil(this);
							if(istpl)
							{
								sqltpl = new ESTemplate(sqlinfo);
								sqlinfo.setEstpl(sqltpl);
								BBossVelocityUtil.initElasticTemplate(sqltpl);
								try {
									sqltpl.process();
								}
								catch (Exception e){
									log.error(sqlinfo.getTemplate(),e);
								}
							}
							
							esInfos.put(key, sqlinfo);
						}
					}
				}
				else
				{
					String templateName = pro.getReferenceTemplateName();//(String)pro.getExtendAttribute("templateName");
					if(templateName == null)
					{
						log.warn(new StringBuilder().append("The DSL template ")
													 .append(key).append(" in the DSl file ")
											.append(templatecontext.getNamespace())
											.append(" is defined as a reference to the DSL template in another configuration file ")
											.append(templateFile)
											.append(", but the name of the DSL template statement to be referenced is not specified by the templateName attribute, for example:\r\n")
											.append("<property name= \"querySqlTraces\"\r\n")
											.append("templateFile= \"esmapper/estrace/ESTracesMapper.xml\"\r\n")
											.append("templateName= \"queryTracesByCriteria\"/>").toString());
					}
					else
					{
						esrefs.put(key, new ESRef(templateName,templateFile,key,dslMappingDir));
						hasrefs = true;
					}
				}
			}
		}
		this.esInfos = esInfos;
		this.esrefs = esrefs;
	}
	
	public boolean hasrefs()
	{
		return this.hasrefs;
	}
	
	void _destroy()
	{
		destroyed = true;
		if(esInfos != null)
		{
			this.esInfos.clear();
			esInfos = null;
		}
		if(esrefs != null)
		{
			this.esrefs.clear();
			esrefs = null;
		}
		this.templateCache.clear();
		if(templatecontext != null)
			templatecontext.destroy(true);
		
	 
		
	}

	/**
	void clearTemplateDatas(){
		if (esInfos != null) {
			this.esInfos.clear();
			esInfos = null;
		}
		if (esrefs != null) {
			this.esrefs.clear();
			esrefs = null;
		}
		this.templateCache.clear();
	}*/
	void buildTemplateDatas(TemplateContainer newTemplateContainer){
		this.templatecontext = newTemplateContainer;
		trimValues();
		this.perKeyDSLStructionCacheSize = templatecontext.getPerKeyDSLStructionCacheSize();//("perKeyDSLStructionCacheSize",ESUtil.defaultPerKeyDSLStructionCacheSize);
		this.alwaysCacheDslStruction  = templatecontext.isAlwaysCacheDslStruction();//getBooleanProperty("alwaysCacheDslStruction",ESUtil.defaultAlwaysCacheDslStruction);
		templateCache = new ESTemplateCache(perKeyDSLStructionCacheSize,alwaysCacheDslStruction);
		destroyed = false;
	}
	void reinit()
	{
		templatecontext.reinit(this);

		/**
		String file = templatecontext.getConfigfile();
		templatecontext.removeCacheContext();
		ESSOAFileApplicationContext essoaFileApplicationContext = new ESSOAFileApplicationContext(file);
		if(essoaFileApplicationContext.getParserError() == null) {
			if (esInfos != null) {
				this.esInfos.clear();
				esInfos = null;
			}
			if (esrefs != null) {
				this.esrefs.clear();
				esrefs = null;
			}
			this.templateCache.clear();
			templatecontext.destroy(false);
			templatecontext = essoaFileApplicationContext;
//			templatecontext = new ESSOAFileApplicationContext(file);
			trimValues();
			destroyed = false;
		}
		else{
			templatecontext.restoreCacheContext();
		}
		 */
		
	}
	 
	static class ResourceTempateRefresh implements ResourceInitial
	{
		private ESUtil sqlutil ;
		public ResourceTempateRefresh(ESUtil sqlutil)
		{
			this.sqlutil = sqlutil;
		}
		public void reinit() {
			sqlutil.reinit();
		}


	}
	
	public static void stopmonitor()
	{
		try {
			if(ESUtil.damon != null)
			{
				ESUtil.damon.stopped();
				damon = null;
			}
		} catch (Throwable e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
	}
	
	public String getTemplateFile()
	{
		return this.templatecontext.getNamespace();
		
	}
	static
	{
		BaseApplicationContext.addShutdownHook(new Runnable(){

			public void run() {
				ESUtil.stopmonitor();
				destory();
				 
			}});
	}
	private static Object lock = new Object();
	private void checkESUtil(){
		
		refresh_interval = ElasticSearchHelper.getDslfileRefreshInterval();
		if(refresh_interval > 0)
		{
			if(damon == null)
			{
				synchronized(lock)
				{
					if(damon == null)
					{
						damon = new DaemonThread(refresh_interval,"ElasticSearch DSL Template Refresh Worker");
						damon.start();
						
					}
				}
			}
			templatecontext.monitor(damon,new ResourceTempateRefresh(this));
//			damon.addFile(fileUrl,templateNamespace, new ResourceTempateRefresh(sqlutil));
		}
		else{
			log.debug("ElasticSearch DSL Template Refresh Interval:"+refresh_interval+",ignore hotload DSL Template["+templatecontext.getNamespace()+"]");
		}
		
	}

	private ESUtil(String dslMappingDir,String templatefile) {
		this.templateFile = templatefile;
		this.dslMappingDir = dslMappingDir;

		this.templatecontext = new AOPTemplateContainerImpl(dslMappingDir,new ESSOAFileApplicationContext(dslMappingDir,templatefile));
		this.perKeyDSLStructionCacheSize = templatecontext.getPerKeyDSLStructionCacheSize();//("perKeyDSLStructionCacheSize",ESUtil.defaultPerKeyDSLStructionCacheSize);
		this.alwaysCacheDslStruction  = templatecontext.isAlwaysCacheDslStruction();//getBooleanProperty("alwaysCacheDslStruction",ESUtil.defaultAlwaysCacheDslStruction);
		templateCache = new ESTemplateCache(perKeyDSLStructionCacheSize,alwaysCacheDslStruction);
		this.realTemplateFile = this.templatecontext.getNamespace();//.getConfigfile();
		this.trimValues();
		checkESUtil();
		
 
	}

	private ESUtil(TemplateContainer templateContainer) {
//		this.templateFile = templatefile;
		this.templatecontext = templateContainer;//new AOPTemplateContainerImpl(this,new ESSOAFileApplicationContext(templatefile));
		this.perKeyDSLStructionCacheSize = templatecontext.getPerKeyDSLStructionCacheSize();//("perKeyDSLStructionCacheSize",ESUtil.defaultPerKeyDSLStructionCacheSize);
		this.alwaysCacheDslStruction  = templatecontext.isAlwaysCacheDslStruction();//getBooleanProperty("alwaysCacheDslStruction",ESUtil.defaultAlwaysCacheDslStruction);
		templateCache = new ESTemplateCache(perKeyDSLStructionCacheSize,alwaysCacheDslStruction);
		this.realTemplateFile = this.templatecontext.getNamespace();//.getConfigfile();
		this.trimValues();
		checkESUtil();


	}



	public static ESUtil getInstance(TemplateContainer templateContainer) {
		String namespace = templateContainer.getNamespace();
		ESUtil sqlUtil = esutils.get(namespace);
		if(sqlUtil != null)
			return sqlUtil;
		synchronized(esutils)
		{
			sqlUtil = esutils.get(namespace);
			if(sqlUtil != null)
				return sqlUtil;
			sqlUtil = new ESUtil(templateContainer);

			esutils.put(namespace, sqlUtil);

		}

		return sqlUtil;
	}


	public static ESUtil getInstance( String templateFile){
		return getInstance(ElasticSearchHelper.getDslfileMappingDir(), templateFile);
	}
	public static ESUtil getInstance(String dslMappingDir, String templateFile) {
		
		ESUtil sqlUtil = esutils.get(templateFile);
		if(sqlUtil != null)
			return sqlUtil;
		synchronized(esutils)
		{
			sqlUtil = esutils.get(templateFile);
			if(sqlUtil != null)
				return sqlUtil;
			sqlUtil = new ESUtil(  dslMappingDir,templateFile);
			
			esutils.put(templateFile, sqlUtil);
			
		}
		
		return sqlUtil;
	}
	
	static void destory()
	{
		if(esutils != null)
		{
			Iterator<Map.Entry<String,ESUtil>> it = esutils.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry<String,ESUtil> entry = it.next();
				entry.getValue()._destroy();
			}
			esutils.clear();
			esutils = null;
		}
	}
	
	private ESInfo getReferESInfo( String templateName)
	{
		if(assertDestoried())
			return null;
		ESRef ref = this.esrefs != null ?this.esrefs.get(templateName):null;
		if(ref != null)
			return ref.getESInfo();
		else
			return null;
	}

	public ESInfo getESInfo(  String templateName) {
		if(templateName == null || templateName.equals("")){
			throw new ElasticsearchParseException("Dsl templateName cann't been empty or null.");
		}
		if(assertDestoried())
			return null;
		ESInfo sql = null;
		if(this.hasrefs)
		{
			sql = this.getReferESInfo(templateName);
			if(sql != null)
				return sql;
		}

		if(esInfos != null)
			sql = this.esInfos.get(templateName);
		
		return sql;

	}
	
	public String getPlainTemplate( String templateName) 
	{
		if(assertDestoried())
			return null;
		ESInfo sql = null;
		if(this.hasrefs)
		{
			sql = this.getReferESInfo( templateName);
			if(sql != null)
				return sql.getTemplate();
		}

		if(esInfos != null)
			sql = this.esInfos.get(templateName);
		 
		if(sql != null)
			return sql.getTemplate();
		else
			return null;
	}
	private String getReferTemplate(  String templateName)
	{
		if(assertDestoried())
			return null;
		ESRef ref = this.esrefs != null ?this.esrefs.get(templateName):null;
		if(ref != null)
			return ref.getTemplate();
		else
			return null;
	}
	private boolean assertDestoried(){
		return this.destroyed;
	}

	public String getTemplate( String templateName) {
		if(assertDestoried())
			return null;
		if(this.hasrefs)
		{
			String sql = this.getReferTemplate(templateName);
			if(sql != null)
				return sql;
		}
		 
		ESInfo esInfo = esInfos != null?
					esInfos.get(templateName):null;
		 
		 	
		return esInfo != null?esInfo.getTemplate():null;

	}
	private String getReferTemplate( String templateName,Map variablevalues)
	{
		if(this.assertDestoried()){
			return null;
		}
		ESRef ref = this.esrefs != null ?this.esrefs.get(templateName):null;
		if(ref != null)
			return ref.getTemplate(variablevalues);
		else
			return null;
	}
	
	public String getTemplate( String templateName,Map variablevalues) {
		if(this.assertDestoried()){
			return null;
		}
		if(this.hasrefs)
		{
			String sql = this.getReferTemplate(templateName,variablevalues);
			if(sql != null)
				return sql;
		}
		 
		String newsql = null;
		ESInfo sql =  this.esInfos != null ?this.esInfos.get(templateName):null;
		
		
		if(sql != null )
		{
			newsql = _getTemplate(sql,variablevalues);
			
		}
		return newsql;

	}
	
	public static String _getTemplate(ESInfo sqlinfo,Map variablevalues)
	{
//		String newsql = null;
//		if(sqlinfo.istpl() )
//		{
//			StringWriter sw = new StringWriter();
//			sqlinfo.getSqltpl().merge(BBossVelocityUtil.buildVelocityContext(variablevalues),sw);
//			newsql = sw.toString();
//		}
//		else
//			newsql = sqlinfo.getSql();
//		return newsql;
		
		
		String sql = null;
    	VelocityContext vcontext = null;
    	if(sqlinfo.isTpl())
    	{
    		sqlinfo.getEstpl().process();//识别sql语句是不是真正的velocity sql模板
    		if(sqlinfo.isTpl())
    		{
    			vcontext = BBossVelocityUtil.buildVelocityContext(variablevalues);//一个context是否可以被同时用于多次运算呢？
		    	
		    	StringWriter sw = new StringWriter();
		       sqlinfo.getEstpl().merge(vcontext,sw);
		       sql = sw.toString();
    		}
    		else
    		{
    			sql = sqlinfo.getTemplate();
    		}
	    	
    	}
    	else
    	{
    		sql = sqlinfo.getTemplate();
    	}
    	return sql;
	}
	/**
	 * mark 1
	 * @param name
	 * @param sql
	 * @param variablevalues
	 * @return
	 */
	public String evaluateSQL(String name,String sql,Map variablevalues) {
		
		if(sql != null &&  variablevalues != null && variablevalues.size() > 0)
		{
			sql = BBossVelocityUtil.evaluate(variablevalues, this.templatecontext.getNamespace()+"|"+name, sql);
		}
		return sql;

	}
	
	 

	 
	
	public String[] getPropertyKeys()
	{
		Set<String> keys = this.templatecontext.getTempalteNames();
		if(keys == null )
			return new String[]{};
		String[] rets = new String[keys.size()];
		Iterator<String> its = keys.iterator();
		int i = 0;
		while(its.hasNext())
		{
			rets[i] = its.next();
			i ++;
		}
		
		return rets;
	}

	 
	 
 
	  

	/**
	 * @return the sqlcontext
	 */
	public TemplateContainer getTemplateContext() {
		return this.templatecontext;
	}
	
	/**
	 * @return the refresh_interval
	 */
	public long getRefresh_interval() {
		return refresh_interval;
	}
	
	public static List<String> getTemplateFiles()
	{
		Iterator<String> it = esutils.keySet().iterator();
		List<String> files = new ArrayList<String>();
		while(it.hasNext())
			files.add(it.next());
		return files;
	}


 

	public boolean fromConfig() {
		
		return this.templatecontext != null;
	}

	public String getRealTemplateFile() {
		return realTemplateFile;
	}

}
