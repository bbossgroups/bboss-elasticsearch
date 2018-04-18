package org.frameworkset.elasticsearch.template;/*
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

import bboss.org.apache.velocity.VelocityContext;
import com.frameworkset.util.VariableHandler;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.util.ClassUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class ESTemplateHelper {
	private static String evalNullParamsTemplate(ESUtil esUtil,String templateName,ESInfo esInfo){
		if(!esInfo.isTpl()) {
			return esInfo.getTemplate();
		}
		else{
			ESTemplate esTemplate = esInfo.getEstpl();
			esTemplate.process();
			if (esInfo.isTpl()) {
				VelocityContext vcontext = esUtil.buildVelocityContext();//一个context是否可以被同时用于多次运算呢？
				BBossStringWriter sw = new BBossStringWriter();
				esTemplate.merge(vcontext, sw);
				return sw.toString();
			}
			else
			{
				return esInfo.getTemplate();
			}
		}
	}

	public static String evalTemplate(ESUtil esUtil,String templateName, Map params)  {

		ESInfo esInfo = esUtil.getESInfo(templateName);
		if (esInfo == null)
			throw new ElasticSearchException("ElasticSearch Template [" + templateName + "]@" + esUtil.getRealTemplateFile() + " 未定义.");
		if (params == null || params.size() == 0) {
//			if(!esInfo.isTpl()) {
//				return esInfo.getTemplate();
//			}
//			else{
//				ESTemplate esTemplate = esInfo.getEstpl();
//				esTemplate.process();
//				if (esInfo.isTpl()) {
//					VelocityContext vcontext = esUtil.buildVelocityContext();//一个context是否可以被同时用于多次运算呢？
//					BBossStringWriter sw = new BBossStringWriter();
//					esTemplate.merge(vcontext, sw);
//					return sw.toString();
//				}
//				else
//				{
//					return esInfo.getTemplate();
//				}
//			}
			return evalNullParamsTemplate(esUtil,templateName,esInfo);

		}
		String template = null;
		if (esInfo.isTpl()) {
			ESTemplate esTemplate = esInfo.getEstpl();
			esTemplate.process();//识别sql语句是不是真正的velocity sql模板
			if (esInfo.isTpl()) {
				VelocityContext vcontext = esUtil.buildVelocityContext(params);//一个context是否可以被同时用于多次运算呢？,已经被转义处理

				BBossStringWriter sw = new BBossStringWriter();
				esTemplate.merge(vcontext, sw);
//				template = sw.toString();
				StringBuilder builder = new StringBuilder();
				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(sw.toString());
				template = evalDocumentStruction(    esUtil,builder,  struction ,  params,  templateName,  null);
			} else {
//				template = esInfo.getTemplate();
				StringBuilder builder = new StringBuilder();
				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
				template = evalDocumentStruction(  esUtil,  builder,  struction ,  params,  templateName,  null);
			}

		} else {
//			template = esInfo.getTemplate();
			StringBuilder builder = new StringBuilder();
			VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
			template = evalDocumentStruction(   esUtil, builder,  struction ,  params,  templateName,  null);
		}

		return template;
		//return templateName;
	}
	public static  Object getId(Object bean){
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(bean.getClass());
		ClassUtil.PropertieDescription pkProperty = beanInfo.getPkProperty();
		if(pkProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,pkProperty.getName());
	}
	public static String evalTemplate(ESUtil esUtil,String templateName, Object params) {

		ESInfo esInfo = esUtil.getESInfo(templateName);
		if (esInfo == null)
			throw new ElasticSearchException("ElasticSearch Template [" + templateName + "]@" + esUtil.getRealTemplateFile() + " 未定义.");
		if (params == null) {
//			return esInfo.getTemplate();
			return evalNullParamsTemplate(esUtil,templateName,esInfo);
		}
		String template = null;
		if (esInfo.isTpl()) {
			esInfo.getEstpl().process();//识别sql语句是不是真正的velocity sql模板
			if (esInfo.isTpl()) {
				VelocityContext vcontext = esUtil.buildVelocityContext(params);//一个context是否可以被同时用于多次运算呢？

				BBossStringWriter sw = new BBossStringWriter();
				esInfo.getEstpl().merge(vcontext, sw);

				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(sw.toString());
				StringBuilder builder = new StringBuilder();
//				template = evalDocumentStruction(   esUtil,builder,  struction ,  vcontext.getContext(),  templateName,  null,true);
				template = evalDocumentStruction(   esUtil,builder,  struction ,  params,  templateName,  null);
			} else {
//				template = esInfo.getTemplate();
				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
				StringBuilder builder = new StringBuilder();
				template = evalDocumentStruction(   esUtil, builder,  struction ,  params,  templateName,  null);
			}

		} else {
//			template = esInfo.getTemplate();
			VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
			StringBuilder builder = new StringBuilder();
			template = evalDocumentStruction(  esUtil,  builder,  struction ,  params,  templateName,  null);
//			template = builder.toString();
		}

		return template;
		//return templateName;
	}


	public static  void buildMeta(StringBuilder builder ,String indexType,String indexName, Object params,String action){
		Object id = getId(params);
		if(id != null)
			builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : \"").append(id).append("\" } }\n");
		else
			builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\" } }\n");
	}
	public static  void buildMeta(Writer writer , String indexType, String indexName, Object params, String action) throws IOException {
		Object id = getId(params);
		if(id != null) {
			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\", \"_id\" : \"");
			writer.write(String.valueOf(id));
			writer.write("\" } }\n");
		}
		else {

			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\" } }\n");
		}
	}
	public static void evalBuilk( Writer writer,String indexName, String indexType, Object param, String action) throws IOException {

		if (param != null) {
			buildMeta(  writer ,  indexType,  indexName,   param,action);
			if(!action.equals("update")) {
				SerialUtil.object2json(param,writer);
				writer.write("\n");
			}
			else
			{
				writer.write("{\"doc\":");
				SerialUtil.object2json(param,writer);
				writer.write("}\n");
			}
		}

	}
	public static void evalBuilkTemplate(ESUtil esUtil,StringBuilder builder ,String indexName,String indexType,String templateName, Object params,String action) {

		ESInfo esInfo = esUtil.getESInfo(templateName);
		if (esInfo == null)
			throw new ElasticSearchException("ElasticSearch Template [" + templateName + "]@" + esUtil.getRealTemplateFile() + " 未定义.");
		if (params == null) {
			buildMeta(  builder ,  indexType,  indexName,   params,action);
			String template = ESTemplateHelper.evalNullParamsTemplate(esUtil,templateName,esInfo);
			if(!action.equals("update"))
				builder.append(template).append("\n");
			else
			{
				builder.append("{\"doc\":").append(template).append("}\n");
			}
			return;
		}
		if (esInfo.isTpl()) {
			esInfo.getEstpl().process();//识别sql语句是不是真正的velocity sql模板

			if (esInfo.isTpl()) {
				buildMeta(  builder ,  indexType,  indexName,   params,action);
				VelocityContext vcontext = esUtil.buildVelocityContext(params);//一个context是否可以被同时用于多次运算呢？
				BBossStringWriter sw = new BBossStringWriter();
				esInfo.getEstpl().merge(vcontext, sw);
				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(sw.toString());
				evalStruction(  esUtil,  builder,  struction ,  params,  templateName,  action);
			} else {
				buildMeta(  builder ,  indexType,  indexName,   params,action);
				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
				evalStruction(  esUtil,  builder,  struction ,  params,  templateName,  action);
			}

		} else {
			buildMeta(  builder ,  indexType,  indexName,   params,action);
			VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
			evalStruction(    esUtil,builder,  struction ,  params,  templateName,  action);
		}

		//return templateName;
	}

	public static String evalDocumentTemplate(ESUtil esUtil,StringBuilder builder ,String indexType,String indexName,String templateName, Object params,String action) {

		ESInfo esInfo = esUtil.getESInfo(templateName);
		if (esInfo == null)
			throw new ElasticSearchException("ElasticSearch Template [" + templateName + "]@" + esUtil.getRealTemplateFile() + " 未定义.");
		if (params == null) {
			String template = ESTemplateHelper.evalNullParamsTemplate(esUtil,templateName,esInfo);
			return template;
		}
		if (esInfo.isTpl()) {
			esInfo.getEstpl().process();//识别sql语句是不是真正的velocity sql模板

			if (esInfo.isTpl()) {

				VelocityContext vcontext = esUtil.buildVelocityContext(params);//一个context是否可以被同时用于多次运算呢？
				BBossStringWriter sw = new BBossStringWriter(builder);
				esInfo.getEstpl().merge(vcontext, sw);
				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(sw.toString());
				builder.setLength(0);
				return evalDocumentStruction(   esUtil, builder,  struction ,  params,  templateName,  action);
			} else {

				VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
				return evalDocumentStruction(   esUtil, builder,  struction ,  params,  templateName,  action);
			}

		} else {
			VariableHandler.URLStruction struction = esInfo.getTemplateStruction(esInfo.getTemplate());
			return evalDocumentStruction(   esUtil, builder,  struction ,  params,  templateName,  action);
		}

		//return templateName;
	}

	public static void evalStruction(ESUtil esUtil,StringBuilder builder,VariableHandler.URLStruction struction ,Object params,String templateName,String action){
		if(!struction.hasVars()) {
			if(!action.equals("update"))
				builder.append(struction.getUrl()).append("\n");
			else
			{
				builder.append("{\"doc\":").append(struction.getUrl()).append("}\n");
			}
		}
		else
		{
			if(!action.equals("update")) {
				esUtil.evalStruction(builder,struction,params,templateName);
				builder.append("\n");
			}
			else
			{
				builder.append("{\"doc\":");
				esUtil.evalStruction(builder,struction,params,templateName);
				builder.append("}\n");
			}

		}
	}
	public static void evalStruction(ESUtil esUtil,StringBuilder builder,VariableHandler.URLStruction struction ,Map params,String templateName,String action){
		if(!struction.hasVars()) {
			if(!action.equals("update"))
				builder.append(struction.getUrl()).append("\n");
			else
			{
				builder.append("{\"doc\":").append(struction.getUrl()).append("}\n");
			}
		}
		else
		{
			if(!action.equals("update")) {
				esUtil.evalStruction(builder,struction,params,templateName);
				builder.append("\n");
			}
			else
			{
				builder.append("{\"doc\":");
				esUtil.evalStruction(builder,struction,params,templateName);
				builder.append("}\n");
			}

		}
	}

	public static String evalDocumentStruction(ESUtil esUtil,StringBuilder builder,VariableHandler.URLStruction struction ,Map params,String templateName,String action){
		if(!struction.hasVars()) {

			return struction.getUrl();

		}
		else
		{
			esUtil.evalStruction(builder,struction,params,templateName);
			return builder.toString();
		}
	}
	public static String evalDocumentStruction(ESUtil esUtil,StringBuilder builder,VariableHandler.URLStruction struction ,Object params,String templateName,String action){
		if(!struction.hasVars()) {

			return struction.getUrl();

		}
		else
		{
			esUtil.evalStruction(builder,struction,params,templateName);
			return builder.toString();
		}
	}


}
