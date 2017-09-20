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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.assemble.Pro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.frameworkset.util.DaemonThread;
import com.frameworkset.util.ResourceInitial;
import com.frameworkset.velocity.BBossVelocityUtil;

import bboss.org.apache.velocity.VelocityContext;

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
	protected Map<String,ESInfo> esInfos;
	protected Map<String,ESRef> esrefs;
	protected boolean hasrefs;
	protected String templateFile;
	protected String realTemplateFile;
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
