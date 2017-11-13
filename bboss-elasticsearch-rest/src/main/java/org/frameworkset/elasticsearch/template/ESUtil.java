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
import org.frameworkset.elasticsearch.ElasticsearchParseException;
import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.Param;
import org.frameworkset.spi.assemble.Pro;
import org.frameworkset.util.ClassUtil;
import org.frameworkset.util.annotations.DateFormateMeta;
import org.frameworkset.util.annotations.wraper.ColumnWraper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
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
	protected BaseApplicationContext templatecontext;
	private static Logger log = LoggerFactory.getLogger(ESUtil.class);
	protected static Map<String,ESUtil> esutils = new HashMap<String,ESUtil>(); 
	protected static long refresh_interval = 5000;
	protected  ESTemplateCache templateCache = new ESTemplateCache();
	protected Map<String,ESInfo> esInfos;
	protected Map<String,ESRef> esrefs;
	protected boolean hasrefs;
	protected String templateFile;
	protected String realTemplateFile;
	private static String java_date_format = "yyyy-MM-dd HH:mm:ss.SSS";
	public VariableHandler.URLStruction getTempateStruction(ESInfo esInfo, String template) {
		return this.templateCache.getTemplateStruction(esInfo,template);
	}
	public String getJavaDateFormat() {
		return java_date_format;
	}

	public VelocityContext buildVelocityContext(Object bean) {
		VelocityContext context_ = new VelocityContext();
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		String name = null;
		DateFormateMeta dataformat = null;

//		String charset = null;
		Object value = null;
		Class type = null;

//		Method writeMethod = null;

		List<ClassUtil.PropertieDescription> attributes = beanInfo.getPropertyDescriptors();
		for (int i = 0; attributes != null && i < attributes.size(); i++) {
			ClassUtil.PropertieDescription property = attributes.get(i);
			ColumnWraper column = property.getColumn();
			if (column != null && (column.ignoreCUDbind() || column.ignorebind()))
				continue;

			type = property.getPropertyType();


			try {
				if (property.canread()) {
					try {
						value = property.getValue(bean);
					} catch (InvocationTargetException e1) {
						log.error("获取属性[" + beanInfo.getClazz().getName() + "." + property.getName() + "]值失败：", e1.getTargetException());
					} catch (Exception e1) {
						log.error("获取属性[" + beanInfo.getClazz().getName() + "." + property.getName() + "]值失败：", e1);
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
								type = value.getClass();

							} else {
								type = ((ColumnType) cv).getType();
							}
						}

					}
					if (value == null) {
						context_.put(name, null);
					} else if (value instanceof Date) {
						if(dataformat != null)
							context_.put(name, this.getDate((Date) value, dataformat));
						else
							context_.put(name, ((Date) value).getTime());
					} else if(value instanceof String){
						CharEscapeUtil charEscapeUtil = new CharEscapeUtil();
						charEscapeUtil.writeString((String)value,false);
						context_.put(name, charEscapeUtil.toString());
					}
					else {
						context_.put(name, value);
					}
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



	public VelocityContext buildVelocityContext(Map data) {

		VelocityContext context_ = new VelocityContext();
		Iterator<Map.Entry<String, Param>> it = data.entrySet().iterator();
		Object temp = null;
		while (it.hasNext()) {
			Map.Entry<String, Param> entry = it.next();
			temp = entry.getValue();
			
			if (temp != null){
				if(temp instanceof String) {
					CharEscapeUtil charEscapeUtil = new CharEscapeUtil();
					charEscapeUtil.writeString((String) temp,false);
					temp = charEscapeUtil.toString();
				}
				context_.put(entry.getKey(), temp);
			}
		}

		return context_;

	}
	public String getDate(Date date, DateFormateMeta dateFormateMeta){
		String format = null;
		if (dateFormateMeta == null) {
			format = this.getJavaDateFormat();
		} else
			format = dateFormateMeta.getDateformat();
		SimpleDateFormat f = null;
		if(dateFormateMeta == null )
			f = new SimpleDateFormat(format) ;
		else {
			if(dateFormateMeta.getLocale() == null)
				f = new SimpleDateFormat(format);
			else
				f = new SimpleDateFormat(format, dateFormateMeta.getLocale());

		}
		try {
			String _date = f.format(date);

			return _date;
		}
		catch (Exception e) {
			throw new ElasticSearchException(e);
		}
	}
	public void getVariableValue(StringBuilder builder,VariableHandler.Variable variable,Object bean,List<ClassUtil.PropertieDescription> attributes,ClassUtil.ClassInfo beanInfo,String template) {
//		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		String name = null;
		DateFormateMeta dataformat = null;

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
						log.error(new StringBuilder().append("获取属性[" ).append( beanInfo.getClazz().getName() ).append( "." + property.getName() ).append( "]值失败：请检查模板定义").append("[")
								.append(template).append("]@").append(this.templatecontext.getConfigfile()).toString(), e1.getTargetException());
					} catch (Exception e1) {
						log.error(new StringBuilder().append("获取属性[" ).append( beanInfo.getClazz().getName() ).append( "." + property.getName() ).append( "]值失败：请检查模板定义").append("[")
								.append(template).append("]@").append(this.templatecontext.getConfigfile()).toString(), e1);
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
								throw new ElasticSearchException(new StringBuilder().append("转换属性[" ).append( beanInfo.getClazz().getName() ).append( "." ).append( property.getName() ).append( "]值失败：值为null时，转换器必须返回ColumnType类型的对象,用来指示表字段对应的java类型。请检查模板定义").append("[")
										.append(template).append("]@").append(this.templatecontext.getConfigfile()).toString());

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
						builder.append("\"").append(this.getDate((Date) value, dataformat)).append("\"");
					} else {
						value = VariableHandler.evaluateVariableValue(variable, value);
						if(value instanceof String){
							builder.append("\"");
//							if(escapeValue){
								CharEscapeUtil charEscapeUtil = new CharEscapeUtil(new BBossStringWriter(builder));
								charEscapeUtil.writeString((String)value,true);
//							}
//							else {
//								builder.append(value.toString());
//							}
							builder.append("\"");
						}
						else if (value instanceof Date) {
							builder.append("\"").append(this.getDate((Date) value, dataformat)).append("\"");
						}
						else
							builder.append(value.toString());
					}
//					params.addSQLParamWithDateFormateMeta(name, value, sqltype, dataformat,charset);

				}
				name = null;
				value = null;
				dataformat = null;

				return;

			} catch (SecurityException e) {
				throw new ElasticSearchException(new StringBuilder().append("转换属性值失败：请检查模板定义").append("[")
						.append(template).append("]@").append(this.templatecontext.getConfigfile()).toString(),e);
			} catch (IllegalArgumentException e) {
				throw new ElasticSearchException(new StringBuilder().append("转换属性值失败：请检查模板定义").append("[")
						.append(template).append("]@").append(this.templatecontext.getConfigfile()).toString(),e);
			}
//			catch (InvocationTargetException e) {
//				throw new ElasticSearchException(e.getTargetException());
//			}


			catch (Exception e) {
				throw new ElasticSearchException(e);
			}


		}
		throw new ElasticsearchParseException(new StringBuilder().append(beanInfo.getClazz().getName()).append("没有为elasticsearch模板[")
				.append(template).append("]@").append(this.templatecontext.getConfigfile())
				.append("指定变量值[").append(variable.getVariableName()).append("]").toString());





	}

	public void evalStruction(StringBuilder builder,VariableHandler.URLStruction templateStruction,Object bean,String template){
		List<String> tokens = templateStruction.getTokens();
		List<VariableHandler.Variable> variables = templateStruction.getVariables();
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		List<ClassUtil.PropertieDescription> attributes = beanInfo.getPropertyDescriptors();
		for(int i = 0; i < tokens.size(); i ++){
			builder.append(tokens.get(i));
			if(i < variables.size()) {
				VariableHandler.Variable variable = variables.get(i);
				this.getVariableValue(builder, variable, bean, attributes, beanInfo, template);
			}

		}
	}

	public void evalStruction(StringBuilder builder,VariableHandler.URLStruction templateStruction,Map bean,String template,boolean escapeValue){
		List<String> tokens = templateStruction.getTokens();
		List<VariableHandler.Variable> variables = templateStruction.getVariables();
		for(int i = 0; i < tokens.size(); i ++){
			builder.append(tokens.get(i));
			if(i < variables.size()) {
				VariableHandler.Variable variable = variables.get(i);
				Object data = bean.get(variable.getVariableName());
				if (data == null) {
					if (bean.containsKey(variable.getVariableName()))
						builder.append("null");
					else {
						throw new ElasticsearchParseException(new StringBuilder().append("没有为elasticsearch模板[")
								.append(template).append("]@").append(this.templatecontext.getConfigfile())
								.append("指定变量值[").append(variable.getVariableName()).append("]").toString());
					}
				} else {
					Object value = bean.get(variable.getVariableName());
					if(value instanceof Date){
						builder.append("\"").append(this.getDate((Date) value, null)).append("\"");
					}
					else {

						value = VariableHandler.evaluateVariableValue(variable, value);
						if(value instanceof Date){
							builder.append("\"").append(this.getDate((Date) value, null)).append("\"");
						}
						else if (value instanceof String) {
							if(!escapeValue) {
								builder.append("\"").append((String)value).append("\"");
							}
							else
							{
								builder.append("\"");
								CharEscapeUtil charEscapeUtil = new CharEscapeUtil(new BBossStringWriter(builder));
								charEscapeUtil.writeString((String)value,true);
								builder.append("\"");
							}
						} else
							builder.append(value.toString());
					}
				}
			}

		}

	}

	public static class ESRef
	{
		public ESRef(String esname, String templatefile, String name) {
			super();
			this.esname = esname;
			this.templatefile = templatefile;
			this.name = name;
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
				this.esutil = ESUtil.getInstance(templatefile);
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
	
	private static DaemonThread damon = null; 
	/**
	 * 
	 */
	private void trimValues()
	{
		if(this.templatecontext == null)
			return;
		this.esInfos= null;
		this.esrefs = null;
		esInfos = new HashMap<String,ESInfo>();
		esrefs = new HashMap<String,ESRef> ();
		Set keys = this.templatecontext.getPropertyKeys();
		if(keys != null && keys.size() > 0)
		{
			Iterator<String> keys_it = keys.iterator();
			while(keys_it.hasNext())
			{
				String key = keys_it.next();
				Pro pro = this.templatecontext.getProBean(key);
				String templateFile = (String)pro.getExtendAttribute("templateFile");
				if(templateFile == null)
				{
					Object o = pro.getObject();
					if(o instanceof String)
					{
						
						String value = (String)o;
						
						if(value != null)
						{
							boolean istpl = pro.getBooleanExtendAttribute("istpl",true);//标识sql语句是否为velocity模板
							boolean multiparser = pro.getBooleanExtendAttribute("multiparser",istpl);//如果sql语句为velocity模板，则在批处理时是否需要每条记录都需要分析sql语句
							ESTemplate sqltpl = null;
							value = value.trim();
							ESInfo sqlinfo = new ESInfo(key, value, istpl,multiparser,pro);
							sqlinfo.setEsUtil(this);
							if(istpl)
							{
								sqltpl = new ESTemplate(sqlinfo);
								sqlinfo.setEstpl(sqltpl);
								BBossVelocityUtil.initTemplate(sqltpl);
								sqltpl.process();
							}
							
							esInfos.put(key, sqlinfo);
						}
					}
				}
				else
				{
					String templateName = (String)pro.getExtendAttribute("templateName");
					if(templateName == null)
					{
						log.warn(templatecontext.getConfigfile()+"中name="+key+"的es template被配置为对"+templateFile+"中的templatename引用，但是没有通过templateName设置要引用的es template语句!");
					}
					else
					{
						esrefs.put(key, new ESRef(templateName,templateFile,key));
						hasrefs = true;
					}
				}
			}
		}
	}
	
	public boolean hasrefs()
	{
		return this.hasrefs;
	}
	
	void _destroy()
	{
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
	           
	void reinit()
	{
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
		String file = templatecontext.getConfigfile();
		templatecontext.destroy(true);
		templatecontext = new ESSOAFileApplicationContext(file);		
		 trimValues();

		
		
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
		return this.templatecontext.getConfigfile();
		
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
	private static void checkESUtil(String sqlfile,ESUtil sqlutil){
		
		refresh_interval = BaseApplicationContext.getSQLFileRefreshInterval();
		if(refresh_interval > 0)
		{
			if(damon == null)
			{
				synchronized(lock)
				{
					if(damon == null)
					{
						damon = new DaemonThread(refresh_interval,"ElasticSearch files Refresh Worker"); 
						damon.start();
						
					}
				}
			}
			damon.addFile(sqlfile, new ResourceTempateRefresh(sqlutil));
		}
		
	}
	private ESUtil(String templatefile) {
		this.templateFile = templatefile;
		this.templatecontext = new ESSOAFileApplicationContext(templatefile);		
		this.realTemplateFile = this.templatecontext.getConfigfile();
		this.trimValues();
		
		 checkESUtil(templatefile,this);
		
 
	}
	

	public ESUtil() {
		// TODO Auto-generated constructor stub
	}

	

	public static ESUtil getInstance(String templateFile) {
		
		ESUtil sqlUtil = esutils.get(templateFile);
		if(sqlUtil != null)
			return sqlUtil;
		synchronized(esutils)
		{
			sqlUtil = esutils.get(templateFile);
			if(sqlUtil != null)
				return sqlUtil;
			sqlUtil = new ESUtil(templateFile);
			
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
		ESRef ref = this.esrefs.get(templateName);
		if(ref != null)
			return ref.getESInfo();
		else
			return null;
	}

	public ESInfo getESInfo(  String templateName) {
		ESInfo sql = null;
		if(this.hasrefs)
		{
			sql = this.getReferESInfo(templateName);
			if(sql != null)
				return sql;
		}
		 
		
		sql = this.esInfos.get(templateName);
		
		return sql;

	}
	
	public String getPlainTemplate( String templateName) 
	{
		ESInfo sql = null;
		if(this.hasrefs)
		{
			sql = this.getReferESInfo( templateName);
			if(sql != null)
				return sql.getTemplate();
		}
	 
		
		sql = this.esInfos.get(templateName);
		 
		if(sql != null)
			return sql.getTemplate();
		else
			return null;
	}
	private String getReferTemplate(  String templateName)
	{
		ESRef ref = this.esrefs.get(templateName);
		if(ref != null)
			return ref.getTemplate();
		else
			return null;
	}
	public String getTemplate( String templateName) {
		
		if(this.hasrefs)
		{
			String sql = this.getReferTemplate(templateName);
			if(sql != null)
				return sql;
		}
		 
		ESInfo esInfo = esInfos.get(templateName);
		 
		 	
		return esInfo != null?esInfo.getTemplate():null;

	}
	private String getReferTemplate( String templateName,Map variablevalues)
	{
		ESRef ref = this.esrefs.get(templateName);
		if(ref != null)
			return ref.getTemplate(variablevalues);
		else
			return null;
	}
	
	public String getTemplate( String templateName,Map variablevalues) {
		if(this.hasrefs)
		{
			String sql = this.getReferTemplate(templateName,variablevalues);
			if(sql != null)
				return sql;
		}
		 
		String newsql = null;
		ESInfo sql =  this.esInfos.get(templateName);
		
		
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
			sql = BBossVelocityUtil.evaluate(variablevalues, this.templatecontext.getConfigfile()+"|"+name, sql);
		}
		return sql;

	}
	
	 

	 
	
	public String[] getPropertyKeys()
	{
		Set<String> keys = this.templatecontext.getPropertyKeys();
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
	public BaseApplicationContext getTemplateContext() {
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
