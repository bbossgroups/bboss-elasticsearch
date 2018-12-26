package org.frameworkset.elasticsearch.client;/*
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

import org.frameworkset.elasticsearch.entity.ESIndice;
import org.frameworkset.elasticsearch.entity.IndexField;
import org.frameworkset.elasticsearch.entity.IndiceHeader;
import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.soa.BBossStringWriter;
import org.frameworkset.util.ClassUtil;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.*;

public abstract class BuildTool {
	/**
	 * health status index                         uuid                   pri rep docs.count docs.deleted store.size pri.store.size
	 * @param lineHeader
	 * @return
	 */
	public static Map<Integer,IndiceHeader> buildIndiceHeaders(String lineHeader){
		if(lineHeader == null)
			return null;
		lineHeader = lineHeader.trim();
		Map<Integer,IndiceHeader> indiceHeaders = new HashMap<Integer,IndiceHeader>();
		int k = 0;
		IndiceHeader indiceHeader = null;
		StringBuilder token = new StringBuilder();
		for(int j = 0; j < lineHeader.length(); j ++){
			char c = lineHeader.charAt(j);
			if(c != ' '){
				token.append(c);
			}
			else {
				if(token.length() == 0)
					continue;
				indiceHeader = new IndiceHeader();
				indiceHeader.setHeaderName(token.toString());
				indiceHeader.setPosition(k);
				indiceHeaders.put(k,indiceHeader);
				token.setLength(0);
				k ++;
			}
		}
		if(token.length() > 0){
			indiceHeader = new IndiceHeader();
			indiceHeader.setHeaderName(token.toString());
			indiceHeader.setPosition(k);
			indiceHeaders.put(k,indiceHeader);
			token.setLength(0);
		}
		return indiceHeaders;

	}

	/**
	 * health status index                         uuid                   pri rep docs.count docs.deleted store.size pri.store.size
	 * @param esIndice
	 * @param indiceHeaders
	 * @param position
	 * @param token
	 * @param format
	 */
	private static void putField(ESIndice esIndice,Map<Integer,IndiceHeader> indiceHeaders,int position,StringBuilder token,SimpleDateFormat format){
		IndiceHeader indiceHeader = indiceHeaders.get(position);
		if(indiceHeader.getHeaderName().equals("health")) {
			esIndice.setHealth(token.toString());
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("status")) {
			esIndice.setStatus(token.toString());
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("index")) {
			esIndice.setIndex(token.toString());
			putGendate(  esIndice,  format);
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("uuid")) {
			esIndice.setUuid(token.toString());
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("pri")) {
			esIndice.setPri(Integer.parseInt(token.toString()));
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("rep")) {
			esIndice.setRep(Integer.parseInt(token.toString()));
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("docs.count")) {
			esIndice.setDocsCcount(Long.parseLong(token.toString()));
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("docs.deleted")) {
			esIndice.setDocsDeleted(Long.parseLong(token.toString()));
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("store.size")) {
			esIndice.setStoreSize(token.toString());
			token.setLength(0);
		}
		else if(indiceHeader.getHeaderName().equals("pri.store.size")) {
			esIndice.setPriStoreSize(token.toString());
			token.setLength(0);
		}
		else{
			esIndice.addOtherData(indiceHeader.getHeaderName(),token.toString());
			token.setLength(0);
		}


	}
	public static ESIndice buildESIndice(String line, SimpleDateFormat format,
										 Map<Integer,IndiceHeader> indiceHeaders)
	{
		StringBuilder token = new StringBuilder();
		ESIndice esIndice = new ESIndice();

		int k = 0;
		for(int j = 0; j < line.length(); j ++){
			char c = line.charAt(j);
			if(c != ' '){
				token.append(c);
			}
			else {
				if(token.length() == 0)
					continue;
				putField(esIndice,indiceHeaders,k,token,format);
				k ++;
//				switch (k ){
//					case 0:
//						esIndice.setHealth(token.toString());
//						token.setLength(0);
//						k ++;
//						break;
//					case 1:
//						esIndice.setStatus(token.toString());
//						token.setLength(0);
//						k ++;
//						break;
//					case 2:
//						esIndice.setIndex(token.toString());
//						putGendate(  esIndice,  format);
//						token.setLength(0);
//						k ++;
//						break;
//					case 3:
//						esIndice.setUuid(token.toString());
//						token.setLength(0);
//						k ++;
//						break;
//					case 4:
//						esIndice.setPri(Integer.parseInt(token.toString()));
//						token.setLength(0);
//						k ++;
//						break;
//					case 5:
//						esIndice.setRep(Integer.parseInt(token.toString()));
//						token.setLength(0);
//						k ++;
//						break;
//					case 6:
//						esIndice.setDocsCcount(Long.parseLong(token.toString()));
//						token.setLength(0);
//						k ++;
//						break;
//					case 7:
//						esIndice.setDocsDeleted(Long.parseLong(token.toString()));
//						token.setLength(0);
//						k ++;
//						break;
//					case 8:
//						esIndice.setStoreSize(token.toString());
//						token.setLength(0);
//						k ++;
//						break;
//					case 9:
//						esIndice.setPriStoreSize(token.toString());
//						token.setLength(0);
//						k ++;
//						break;
//					default:
//						break;

//				}
			}
		}
		if(token.length() > 0){
			putField(esIndice,indiceHeaders,k,token,format);
		}
//		esIndice.setPriStoreSize(token.toString());
		return esIndice;
	}
	public static void putGendate(ESIndice esIndice,SimpleDateFormat format){
		int dsplit = esIndice.getIndex().lastIndexOf('-');

		try {
			if(dsplit > 0){
				String date = esIndice.getIndex().substring(dsplit+1);
				esIndice.setGenDate((Date)format.parseObject(date));
			}

		} catch (Exception e) {

		}
	}

	public static String buildGetDocumentRequest(String indexName, String indexType,String documentId,Map<String,Object> options){
		StringBuilder builder = new StringBuilder();
//		builder.append("/").append(indexName).append("/").append(indexType).append("/").append(documentId);
		builder.append("/").append(indexName).append("/").append(indexType).append("/");
		CharEscapeUtil charEscapeUtil = new CharEscapeUtil(new BBossStringWriter(builder));
		charEscapeUtil.writeString(documentId, true);
		if(options != null){
			builder.append("?");
			Iterator<Map.Entry<String, Object>> iterable = options.entrySet().iterator();
			boolean first = true;
			while(iterable.hasNext()){
				Map.Entry<String, Object> entry = iterable.next();
				if(first) {
					builder.append(entry.getKey()).append("=").append(entry.getValue());
					first = false;
				}
				else
				{
					builder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
				}
			}
		}
		return builder.toString();
	}

	public static void buildId(Object id,StringBuilder builder,boolean escape){
		if (id instanceof String) {
			if(!escape) {
				builder.append("\"")
						.append(id).append("\"");
			}
			else{
				builder.append("\"");
				CharEscapeUtil charEscapeUtil = new CharEscapeUtil(new BBossStringWriter(builder));
				charEscapeUtil.writeString((String) id, true);
				builder.append("\"");
			}

		}
		else{
			builder.append(id);
		}
	}
	public static void buildId(Object id,Writer writer,boolean escape) throws IOException {
		if (id instanceof String) {
			writer.write("\"");
			if(!escape) {
				writer.write((String) id);
			}
			else{
				CharEscapeUtil charEscapeUtil = new CharEscapeUtil(writer);
				charEscapeUtil.writeString((String) id, true);
			}
			writer.write("\"");

		}
		else{
			writer.write(String.valueOf(id));
		}
	}

	public static void buildMeta(Writer writer ,String indexType,String indexName, Object params,String action) throws IOException {
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(params.getClass());
		Object id = getId(params,beanInfo);
		Object parentId = getParentId(params,beanInfo);
		Object routing = getRouting(params,beanInfo);
		Object esRetryOnConflict = getEsRetryOnConflict(params,beanInfo);


		buildMeta(  writer ,  indexType,  indexName,   params,  action,  id,  parentId,routing,esRetryOnConflict);
	}


	public static void buildMetaWithDocIdKey(Writer writer ,String indexType,String indexName, Map params,String action,String docIdKey,String parentIdKey) throws IOException {
//		Object id = docIdKey != null ?params.get(docIdKey):null;
//		Object parentId = parentIdKey != null ?params.get(parentIdKey):null;
//		buildMeta(  writer ,  indexType,  indexName,   params,  action,  id,  parentId,null);
		buildMetaWithDocIdKey(writer ,indexType,indexName, params,action,docIdKey,parentIdKey,null);
	}
	public static void buildMetaWithDocIdKey(Writer writer ,String indexType,String indexName, Map params,String action,String docIdKey,String parentIdKey,String routingKey) throws IOException {
		Object id = docIdKey != null ?params.get(docIdKey):null;
		Object parentId = parentIdKey != null ?params.get(parentIdKey):null;
		Object routing = routingKey != null ?params.get(routingKey):null;

		buildMeta(  writer ,  indexType,  indexName,   params,  action,  id,  parentId,routing);
	}
	public static void buildMeta(Writer writer ,String indexType,String indexName, Object params,String action,Object id,Object parentId,Object routing) throws IOException {
		buildMeta(  writer ,  indexType,  indexName,   params,  action,  id,  parentId, routing,null);
	}

	public static void buildMeta(Writer writer ,String indexType,String indexName, Object params,String action,
							 Object id,Object parentId,Object routing,Object esRetryOnConflict) throws IOException {

		if(id != null) {
			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			writer.write("\", \"_id\" : ");
			buildId(id,writer,true);
			if(parentId != null){
				writer.write(", \"parent\" : ");
				buildId(parentId,writer,true);
			}
			if(routing != null){

				writer.write(", \"_routing\" : ");
				buildId(routing,writer,true);
			}

//			if(action.equals("update"))
//			{
			if (esRetryOnConflict != null) {
				writer.write(",\"_retry_on_conflict\":");
				writer.write(String.valueOf(esRetryOnConflict));
			}
			ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(params.getClass());
			ClassUtil.PropertieDescription esVersionProperty = classInfo.getEsVersionProperty();
			if (esVersionProperty != null) {
				Object version = classInfo.getPropertyValue(params,esVersionProperty.getName());
				if(version != null) {
					writer.write(",\"_version\":");

					writer.write(String.valueOf(version));
				}
			}
			ClassUtil.PropertieDescription esVersionTypeProperty = classInfo.getEsVersionTypeProperty();
			if (esVersionTypeProperty != null) {
				Object versionType = classInfo.getPropertyValue(params,esVersionTypeProperty.getName());
				if(versionType != null) {
					writer.write(",\"_version_type\":\"");
					writer.write(String.valueOf(versionType));
					writer.write("\"");
				}
			}
//			}

			writer.write(" } }\n");
		}
		else {

			writer.write("{ \"");
			writer.write(action);
			writer.write("\" : { \"_index\" : \"");
			writer.write(indexName);
			writer.write("\", \"_type\" : \"");
			writer.write(indexType);
			if(parentId != null){
				writer.write(", \"parent\" : ");
				buildId(parentId,writer,true);
			}
			if(routing != null){

				writer.write(", \"_routing\" : ");
				buildId(routing,writer,true);
			}
//			if(action.equals("update"))
//			{
			{
				if (esRetryOnConflict != null) {
					writer.write(",\"_retry_on_conflict\":");
					writer.write(String.valueOf(esRetryOnConflict));
				}
				ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(params.getClass());
				ClassUtil.PropertieDescription esVersionProperty = classInfo.getEsVersionProperty();
				if (esVersionProperty != null) {
					Object version = classInfo.getPropertyValue(params,esVersionProperty.getName());
					if(version != null) {
						writer.write(",\"_version\":");

						writer.write(String.valueOf(version));
					}
				}
				ClassUtil.PropertieDescription esVersionTypeProperty = classInfo.getEsVersionTypeProperty();
				if (esVersionTypeProperty != null) {
					Object versionType = classInfo.getPropertyValue(params,esVersionTypeProperty.getName());
					if(versionType != null) {
						writer.write(",\"_version_type\":\"");
						writer.write(String.valueOf(versionType));
						writer.write("\"");
					}
				}
			}
//			}
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
				ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(param.getClass());
				ClassUtil.PropertieDescription esDocAsUpsertProperty = classInfo.getEsDocAsUpsertProperty();


				ClassUtil.PropertieDescription esReturnSourceProperty = classInfo.getEsReturnSourceProperty();

				writer.write("{\"doc\":");
				SerialUtil.object2json(param,writer);
				if(esDocAsUpsertProperty != null){
					Object esDocAsUpsert = classInfo.getPropertyValue(param,esDocAsUpsertProperty.getName());
					if(esDocAsUpsert != null){
						writer.write(",\"doc_as_upsert\":");
						writer.write(String.valueOf(esDocAsUpsert));
					}
				}
				if(esReturnSourceProperty != null){
					Object returnSource = classInfo.getPropertyValue(param,esReturnSourceProperty.getName());
					if(returnSource != null){
						writer.write(",\"_source\":");
						writer.write(String.valueOf(returnSource));
					}
				}
				writer.write("}\n");



			}
		}

	}

	public static void buildMeta(StringBuilder builder ,String indexType,String indexName, Object params,String action){
		ClassUtil.ClassInfo beanInfo = ClassUtil.getClassInfo(params.getClass());
		Object id = getId(params,  beanInfo );
		Object parentId = getParentId(params,  beanInfo );
		if(id != null) {
			builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName)
					.append("\", \"_type\" : \"").append(indexType).append("\", \"_id\" : ");
			buildId(id,builder,true);
			if(parentId != null){
				builder.append(",\"parent\":");
				buildId(parentId,builder,true);
			}
			builder.append(" } }\n");
		}
		else {
			if(parentId == null)
				builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\" } }\n");
			else{
				builder.append("{ \"").append(action).append("\" : { \"_index\" : \"").append(indexName).append("\", \"_type\" : \"").append(indexType).append("\"");
				builder.append(",\"parent\":");
				buildId(parentId,builder,true);
				builder.append(" } }\n");
			}
		}
	}

	public static void evalBuilk( Writer writer,String indexName, String indexType, Map param, String action,String docIdKey,String parentIdKey) throws IOException {

		if (param != null) {
			buildMetaWithDocIdKey(  writer ,  indexType,  indexName,   param,action,docIdKey,parentIdKey);
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

	public static void handleFields(Map<String,Object> subFileds,String fieldName,List<IndexField> fields){
		if(subFileds == null || subFileds.size() == 0)
			return ;
		Iterator<Map.Entry<String,Object>> iterator = subFileds.entrySet().iterator();
		while(iterator.hasNext()){
			Map.Entry<String,Object> entry = iterator.next();
			IndexField indexField = buildIndexField(entry, fields,fieldName);
		}

	}

	public static Boolean parseBoolean(Object norms){
		if(norms == null){
			return null;
		}
		if(norms instanceof Boolean){
			return (Boolean)norms;
		}
		else if(norms instanceof Map){
			return (Boolean) ((Map) norms).get("enabled");
		}
		return null;
	}
	public static IndexField buildIndexField(Map.Entry<String,Object> field,List<IndexField> fields,String parentFieldName){
//		Map.Entry<String,Object> field = fileds.next();
		IndexField indexField = new IndexField();
		String fieldName = null;
		if(parentFieldName != null){
			fieldName = parentFieldName + "."+field.getKey();
		}
		else {
			fieldName = field.getKey();
		}
		indexField.setFieldName(fieldName);
		Map<String,Object> fieldInfo = (Map<String,Object>)field.getValue();
		indexField.setType((String)fieldInfo.get("type"));
		indexField.setIgnoreAbove(ResultUtil.intValue(fieldInfo.get("ignore_above"),null));
		indexField.setAnalyzer((String)fieldInfo.get("analyzer"));
		indexField.setNormalizer((String)fieldInfo.get("normalizer"));
		indexField.setBoost((Integer)fieldInfo.get("boost"));
		indexField.setCoerce(parseBoolean( fieldInfo.get("coerce")));
		indexField.setCopyTo((String)fieldInfo.get("copy_to"));
		indexField.setDocValues(parseBoolean(fieldInfo.get("doc_values")));//setCoerce();
		indexField.setDynamic(parseBoolean(fieldInfo.get("doc_values")));	//dynamic
		indexField.setEnabled(parseBoolean(fieldInfo.get("enabled")));			//enabled
		indexField.setFielddata(parseBoolean(fieldInfo.get("fielddata")));	//fielddata
		indexField.setFormat((String)fieldInfo.get("format"));		//	format
		indexField.setIgnoreMalformed(parseBoolean(fieldInfo.get("ignore_malformed")));//Coerce();	//		ignore_malformed
		indexField.setIncludeInAll(parseBoolean(fieldInfo.get("include_in_all")));	//include_in_all
		indexField.setIndexOptions((String)fieldInfo.get("index_options"));
		indexField.setIndex(parseBoolean(fieldInfo.get("index")));	//
		indexField.setFields((Map<String,Object>)fieldInfo.get("fields"));	//

		indexField.setNorms(parseBoolean(fieldInfo.get("norms")));//	norms
		indexField.setNullValue(fieldInfo.get("null_value"));	//
		indexField.setPositionIncrementGap((Integer)fieldInfo.get("position_increment_gap"));
		indexField.setProperties((Map<String,Object>)fieldInfo.get("properties"));	//
		indexField.setSearchAnalyzer((String)fieldInfo.get("search_analyzer"));	//search_analyzer
		indexField.setSimilarity((String)fieldInfo.get("similarity"));	//
		indexField.setStore(parseBoolean(fieldInfo.get("store")));	//store
		indexField.setTermVector((String)fieldInfo.get("term_vector"));	//
		fields.add(indexField);
		handleFields(indexField.getFields(), fieldName,fields);
		return indexField;
	}

	public static  Object getId(Object bean,ClassUtil.ClassInfo beanInfo ){

		ClassUtil.PropertieDescription pkProperty = beanInfo.getEsIdProperty();
//		if(pkProperty == null)
//			pkProperty = beanInfo.getPkProperty();
		if(pkProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,pkProperty.getName());
	}

	public static  Object getEsRetryOnConflict(Object bean,ClassUtil.ClassInfo beanInfo ){
		ClassUtil.PropertieDescription esRetryOnConflictProperty = beanInfo.getEsRetryOnConflictProperty();
//		if(pkProperty == null)
//			pkProperty = beanInfo.getPkProperty();
		if(esRetryOnConflictProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,esRetryOnConflictProperty.getName());
	}
	public static  Object getRouting(Object bean,ClassUtil.ClassInfo beanInfo ){
		ClassUtil.PropertieDescription routingProperty = beanInfo.getEsRoutingProperty();
//		if(pkProperty == null)
//			pkProperty = beanInfo.getPkProperty();
		if(routingProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,routingProperty.getName());
	}

	public static  Object getParentId(Object bean,ClassUtil.ClassInfo beanInfo ){
		ClassUtil.PropertieDescription pkProperty = beanInfo.getEsParentProperty();
//		if(pkProperty == null)
//			pkProperty = beanInfo.getPkProperty();
		if(pkProperty == null)
			return null;
		return beanInfo.getPropertyValue(bean,pkProperty.getName());
	}
}
