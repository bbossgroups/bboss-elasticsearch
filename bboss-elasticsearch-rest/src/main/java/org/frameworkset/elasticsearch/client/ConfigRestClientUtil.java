package org.frameworkset.elasticsearch.client;

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.entity.sql.SQLResult;
import org.frameworkset.elasticsearch.entity.suggest.CompleteRestResponse;
import org.frameworkset.elasticsearch.entity.suggest.PhraseRestResponse;
import org.frameworkset.elasticsearch.entity.suggest.TermRestResponse;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;
import org.frameworkset.elasticsearch.template.ESTemplateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 通过配置文件加载dsl模板组件
 * https://esdoc.bbossgroups.com/#/development
 */
public class ConfigRestClientUtil extends AbstractConfigRestClientUtil {
	private static Logger logger = LoggerFactory.getLogger(ConfigRestClientUtil.class);

	public ConfigRestClientUtil(ElasticSearchClient client, IndexNameBuilder indexNameBuilder, String configFile) {
		super(client, indexNameBuilder, configFile);
	}

	public ConfigRestClientUtil(BaseTemplateContainerImpl templateContainer, ElasticSearchClient client, IndexNameBuilder indexNameBuilder) {
		super(templateContainer, client, indexNameBuilder);
	}


	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path,evalTemplate(templateName, params));
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path,evalTemplate(templateName,params));
	}



	@Override
	public String updateByPathWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByPath(path,   evalTemplate(templateName,   params));
	}

	@Override
	public String updateByPathWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByPath(path,  evalTemplate(templateName,  params));
	}

	@Override
	public String deleteByQueryWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteByQuery(path, evalTemplate(templateName, params));
	}

	@Override
	public String deleteByQueryWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteByQuery(path,  evalTemplate(templateName,  params));
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return  ExecuteRequestUtil._addDocuments(clientInterface.getClient(), esUtil, indexName, indexType,addTemplate, beans, refreshOption);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans) throws ElasticSearchException {
		return addDocumentsWithCluster(  datasourceName,indexName,   indexType,  addTemplate,   beans,  null);

	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, Object bean) throws ElasticSearchException {
		return addDocumentWithCluster( datasourceName,  indexName,  indexType,  addTemplate,  bean, (String)null);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return _addDocument(clientInterface.getClient(), indexName,  indexType, addTemplate,  bean, refreshOption);
	}

	//next here.

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String updateTemplate, List<?> beans) throws ElasticSearchException {
		return updateDocumentsWithCluster( datasourceName,  indexName,  indexType,  updateTemplate, beans, (String)null) ;
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return ExecuteRequestUtil._updateDocuments( clientInterface.getClient(), esUtil,indexName, indexType, updateTemplate, beans, refreshOption);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, Object bean) throws ElasticSearchException {
		return addDateDocumentWithCluster(datasourceName, indexName,  indexType,  addTemplate,bean, (String)null);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName,  String indexType,  String addTemplate,Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return addDocumentWithCluster( datasourceName,  clientInterface.getDynamicIndexName(indexName),   indexType,   addTemplate, bean,  refreshOption);
//		return clientInterface.addDateDocument(indexName, indexType, addTemplate, bean, refreshOption);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans) throws ElasticSearchException {
		return addDateDocumentsWithCluster(datasourceName, indexName, indexType, addTemplate, beans, (String)null);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return addDocumentsWithCluster(datasourceName,clientInterface.getDynamicIndexName(indexName), indexType, addTemplate, beans, refreshOption);
	}

	/**
	 *
	 * @param path
	 * @param templateName
	 * @param action
	 * @return
	 * @throws ElasticSearchException
	 */
	@Override
	public String executeHttpWithCluster(String datasourceName,String path, String templateName, String action) throws ElasticSearchException {
		// TODO Auto-generated method stub
//		return this.client.executeHttp(path, evalTemplate(templateName, (Object) null),action);
		return super.executeHttpWithCluster( datasourceName,  path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null),   action);
	}

	@Override
	public <T> T executeHttpWithCluster(String datasourceName, String path, String templateName, String action, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttpWithCluster(datasourceName,path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), action,  responseHandler);
	}

	@Override
	public String executeHttpWithCluster(String datasourceName, String path, String templateName, Map params, String action) throws ElasticSearchException {
		return super.executeHttpWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), action);
	}

	@Override
	public <T> T executeHttpWithCluster(String datasourceName, String path, String templateName, String action, Object bean, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttpWithCluster( datasourceName,  path, ESTemplateHelper.evalTemplate(esUtil,templateName, bean),  action,  responseHandler);
	}

	@Override
	public String executeHttpWithCluster(String datasourceName, String path, String templateName, Object bean, String action) throws ElasticSearchException {
		return super.executeHttpWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName, bean), action);
	}



	@Override
	public String executeRequestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		return super.executeRequestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
	}

	@Override
	public String executeRequestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		return super.executeRequestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName, params));
	}

	@Override
	public <T> T executeRequestWithCluster(String datasourceName, String path, String templateName, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object)null),responseHandler);
	}

	@Override
	public <T> T executeRequestWithCluster(String datasourceName, String path, String templateName, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),responseHandler);
	}

	@Override
	public <T> T executeRequestWithCluster(String datasourceName, String path, String templateName, Object params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),responseHandler);
	}

	@Override
	public MapRestResponse searchWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public MapRestResponse searchWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public MapRestResponse searchWithCluster(String datasourceName, String path, String templateName) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null));
	}

	@Override
	public TermRestResponse termSuggestWithCluster(String datasourceName, String path, String templateName) throws ElasticSearchException {
		return super.termSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null));
	}

	@Override
	public PhraseRestResponse phraseSuggestWithCluster(String datasourceName, String path, String templateName) throws ElasticSearchException {
		return super.phraseSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null));
	}

	@Override
	public TermRestResponse termSuggestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		return super.termSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public PhraseRestResponse phraseSuggestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		return super.phraseSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public TermRestResponse termSuggestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		return super.termSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public PhraseRestResponse phraseSuggestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		return super.phraseSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Class<?> type) throws ElasticSearchException {
		return super.complateSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null),type);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
		return super.complateSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
		return super.complateSuggestWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}

	@Override
	public Map<String, Object> searchMapWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		return super.searchMapWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public Map<String, Object> searchMapWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		return super.searchMapWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}

	@Override
	public Map<String, Object> searchMapWithCluster(String datasourceName, String path, String templateName) throws ElasticSearchException {
		return super.searchMapWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null));
	}


	@Override
	public String createIndiceMappingWithCluster(String datasourceName, String indexName, String indexMappingTemplateName) throws ElasticSearchException {
		return super.createIndiceMappingWithCluster(datasourceName, indexName, ESTemplateHelper.evalTemplate(esUtil,indexMappingTemplateName,(Object)null));
	}

	@Override
	public String updateIndiceMappingWithCluster(String datasourceName, String action, String templateName, Object parameter) throws ElasticSearchException {
		return super.updateIndiceMappingWithCluster(datasourceName, action, ESTemplateHelper.evalTemplate(esUtil,templateName,parameter));
	}

	@Override
	public String createIndiceMappingWithCluster(String datasourceName, String indexName, String templateName, Object parameter) throws ElasticSearchException {
		return super.createIndiceMappingWithCluster(datasourceName, indexName, ESTemplateHelper.evalTemplate(esUtil,templateName,parameter));
	}

	@Override
	public String updateIndiceMappingWithCluster(String datasourceName, String action, String templateName, Map parameter) throws ElasticSearchException {
		return super.updateIndiceMappingWithCluster(datasourceName, action, ESTemplateHelper.evalTemplate(esUtil,templateName,parameter));
	}

	@Override
	public String createIndiceMappingWithCluster(String datasourceName, String indexName, String templateName, Map parameter) throws ElasticSearchException {
		return super.createIndiceMappingWithCluster(datasourceName, indexName, ESTemplateHelper.evalTemplate(esUtil,templateName,parameter));
	}



	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Class<?> type) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object) null),type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, ESTypeReferences type) throws ElasticSearchException {
		return super.searchWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,(Object)null),type);
	}

	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
		return super.searchListWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type);
	}


	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String templateName, String scroll,
											Object params, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		return super.scrollWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),scroll,type,scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName, String path, String templateName, String scroll,
													Object params, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		return super.scrollParallelWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),scroll,type,scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String templateName, String scroll, Object params, Class<T> type) throws ElasticSearchException {
		return super.scrollWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),scroll,type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String templateName, String scroll, Map params, Class<T> type) throws ElasticSearchException {
		return super.scrollWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),scroll,type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String templateName,
											String scroll, Map params, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		return super.scrollWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),scroll,type,scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName, String path, String templateName, String scroll, Map params,
													Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		return super.scrollParallelWithCluster(datasourceName, path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),scroll,type,scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollSliceWithCluster(String datasourceName, String path, String dslTemplate, Map params,
												 String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return ExecuteRequestUtil._scrollSlice(clientInterface.getClient(),esUtil,path, dslTemplate, params ,		scroll  , type,
		 scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollSliceParallelWithCluster(String datasourceName, String path, String dslTemplate, Map params,
														 String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return ExecuteRequestUtil.scrollSliceParallel( clientInterface,esUtil,path, dslTemplate,params ,		 scroll  , type,		scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollSliceWithCluster(String datasourceName, String path, String dslTemplate, Map params, String scroll, Class<T> type) throws ElasticSearchException {
		return scrollSliceWithCluster( datasourceName, path,  dslTemplate, params, scroll ,    type,(ScrollHandler<T>)null);
	}

	@Override
	public <T> ESDatas<T> scrollSliceParallelWithCluster(String datasourceName, String path, String dslTemplate, Map params, String scroll, Class<T> type) throws ElasticSearchException {
		return scrollSliceParallelWithCluster( datasourceName, path,  dslTemplate, params, scroll ,    type,(ScrollHandler<T>)null);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String dslTemplate, String scroll, Class<T> type) throws ElasticSearchException {
		return super.scrollWithCluster(datasourceName,path,ESTemplateHelper.evalTemplate(esUtil,dslTemplate, (Object)null),scroll,type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String dslTemplate, String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		return super.scrollWithCluster(datasourceName,path,ESTemplateHelper.evalTemplate(esUtil,dslTemplate, (Object)null),scroll,type,scrollHandler);
	}


	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		return super.searchListWithCluster( datasourceName, path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
	}

	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, String templateName, Class<T> type) throws ElasticSearchException {
		return super.searchListWithCluster( datasourceName, path,   ESTemplateHelper.evalTemplate(esUtil,templateName),type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
		return super.searchObjectWithCluster( datasourceName, path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		return super.searchObjectWithCluster( datasourceName, path,   ESTemplateHelper.evalTemplate(esUtil,templateName, params),type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, String templateName, Class<T> type) throws ElasticSearchException {
		return super.searchObjectWithCluster( datasourceName, path,   ESTemplateHelper.evalTemplate(esUtil,templateName),type);
	}

//come to here

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Map params,
																 Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type,   aggs,   stats, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Object params,
																 Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type,   aggs,   stats, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName,
																 Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName),   type,   aggs,   stats, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type,   aggs,   stats);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Object params,
																 Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName, params),   type,   aggs,   stats);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName),   type,   aggs,   stats);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Map params,
																 Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName,params),   type,   aggs,aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Object params,
																 Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName,params),   type,   aggs,aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Class<T> type,
																 String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName),   type,   aggs,aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type, String aggs) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName,params),   type,   aggs);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type, String aggs) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName,params),   type,   aggs);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String templateName, Class<T> type, String aggs) throws ElasticSearchException {
		return super.searchAggWithCluster( datasourceName, path,
				ESTemplateHelper.evalTemplate(esUtil,templateName),   type,   aggs);
	}

	@Override
	public String createTempateWithCluster(String datasourceName, String template, String templateName) throws ElasticSearchException {
		return super.createTempateWithCluster(datasourceName,template,ESTemplateHelper.evalTemplate(esUtil,templateName));
	}

	@Override
	public String createTempateWithCluster(String datasourceName, String template, String templateName, Object params) throws ElasticSearchException {
		return super.createTempateWithCluster(datasourceName,template,ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}



	@Override
	public String createTempateWithCluster(String datasourceName, String template, String templateName, Map params) throws ElasticSearchException {
		return super.createTempateWithCluster(datasourceName,template,ESTemplateHelper.evalTemplate(esUtil,templateName,params));
	}


	@Override
	public String updateByQueryWithCluster(String datasourceName, String path, String templateName) throws ElasticSearchException {
		return super.updateByQueryWithCluster(  datasourceName,   path, ESTemplateHelper.evalTemplate(esUtil,templateName)) ;
	}

	@Override
	public String updateByQueryWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		return super.updateByQueryWithCluster(  datasourceName,   path, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public String updateByQueryWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		return super.updateByQueryWithCluster(  datasourceName,   path, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String path, String templateName, Class<T> type) throws ElasticSearchException {
		return super.mgetDocumentsWithCluster(  datasourceName,   path, ESTemplateHelper.evalTemplate(esUtil,templateName),type) ;
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		return super.mgetDocumentsWithCluster(  datasourceName,   path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type) ;
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
		return super.mgetDocumentsWithCluster(  datasourceName,   path, ESTemplateHelper.evalTemplate(esUtil,templateName,params),type) ;
	}

	@Override
	public long countWithCluster(String datasourceName, String index, String templateName) throws ElasticSearchException {
		return super.countWithCluster(  datasourceName,   index, ESTemplateHelper.evalTemplate(esUtil,templateName)) ;
	}

	@Override
	public long countWithCluster(String datasourceName, String index, String templateName, Map params) throws ElasticSearchException {
		return super.countWithCluster(  datasourceName,   index, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public long countWithCluster(String datasourceName, String index, String templateName, Object params) throws ElasticSearchException {
		return super.countWithCluster(  datasourceName,   index, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}


	@Override
	public String reindexByDslWithCluster(String datasourceName, String actionUrl, String templateName, Object params) {
		return super.reindexByDslWithCluster(  datasourceName,   actionUrl, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public String reindexByDslWithCluster(String datasourceName, String actionUrl, String templateName) {
		return super.reindexByDslWithCluster(  datasourceName,   actionUrl, ESTemplateHelper.evalTemplate(esUtil,templateName)) ;
	}



	@Override
	public <T> SQLResult<T> fetchQueryWithCluster(String datasourceName, Class<T> beanType, String templateName, Map params) throws ElasticSearchException {
		return super.fetchQueryWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> SQLResult<T> fetchQueryWithCluster(String datasourceName, Class<T> beanType, String templateName, Object params) throws ElasticSearchException {
		return super.fetchQueryWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> SQLResult<T> fetchQueryWithCluster(String datasourceName, Class<T> beanType, String templateName) throws ElasticSearchException {
		return super.fetchQueryWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName)) ;
	}



	@Override
	public <T> List<T> sqlWithCluster(String datasourceName, Class<T> beanType, String templateName, Map params) throws ElasticSearchException {
		return super.sqlWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> List<T> sqlWithCluster(String datasourceName, Class<T> beanType, String templateName, Object params) throws ElasticSearchException {
		return super.sqlWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> List<T> sqlWithCluster(String datasourceName, Class<T> beanType, String templateName) throws ElasticSearchException {
		return super.sqlWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName)) ;
	}

	@Override
	public <T> T sqlObjectWithCluster(String datasourceName, Class<T> beanType, String templateName, Map params) throws ElasticSearchException {
		return super.sqlObjectWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> T sqlObjectWithCluster(String datasourceName, Class<T> beanType, String templateName, Object params) throws ElasticSearchException {
		return super.sqlObjectWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName,params)) ;
	}

	@Override
	public <T> T sqlObjectWithCluster(String datasourceName, Class<T> beanType, String templateName) throws ElasticSearchException {
		return super.sqlObjectWithCluster(  datasourceName,   beanType, ESTemplateHelper.evalTemplate(esUtil,templateName)) ;
	}





	@Override
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName, String path, String dslTemplate,
													String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		return super.scrollParallelWithCluster(datasourceName,path,ESTemplateHelper.evalTemplate(esUtil,dslTemplate),scroll,type,scrollHandler);
	}



	@Override
	public String createScriptWithCluster(String datasourceName, String scriptName, String dslTemplate) {
		return super.createScriptWithCluster(datasourceName,scriptName,ESTemplateHelper.evalTemplate(esUtil,dslTemplate));

	}

	@Override
	public String createScriptWithCluster(String datasourceName, String scriptName, String scriptDslTemplate, Map params) {
		return super.createScriptWithCluster(datasourceName,scriptName,ESTemplateHelper.evalTemplate(esUtil,scriptDslTemplate,params));
	}

	@Override
	public String createScriptWithCluster(String datasourceName, String scriptName, String scriptDslTemplate, Object params) {
		return super.createScriptWithCluster(datasourceName,scriptName,ESTemplateHelper.evalTemplate(esUtil,scriptDslTemplate,params));
	}

	@Override
	public String addDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		return addDocumentsWithCluster( datasourceName,indexName,   _doc,addTemplate,  beans,refreshOption);
	}

	@Override
	public String addDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans) throws ElasticSearchException {
		return addDocumentsWithCluster( datasourceName,indexName,   _doc,addTemplate,  beans);
	}

	@Override
	public String addDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean) throws ElasticSearchException {
		return addDocumentWithCluster( datasourceName,indexName,    _doc,addTemplate,  bean);
	}

	@Override
	public String addDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException {
		return addDocumentWithCluster( datasourceName,indexName,    _doc,addTemplate,  bean,refreshOption);
	}

	@Override
	public String updateDocumentsNewWithCluster(String datasourceName, String indexName, String updateTemplate, List<?> beans) throws ElasticSearchException {
		return updateDocumentsWithCluster(  datasourceName,indexName,    _doc,   updateTemplate,   beans);
	}

	@Override
	public String updateDocumentsNewWithCluster(String datasourceName, String indexName, String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		return updateDocumentsWithCluster(  datasourceName,indexName,    _doc,   updateTemplate,   beans,refreshOption);
	}



	@Override
	public String addDateDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentNew(indexName, addTemplate,  bean);
	}

	@Override
	public String addDateDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return addDocumentNewWithCluster( datasourceName, clientInterface.getDynamicIndexName( indexName),  addTemplate,  bean,  refreshOption) ;
	}

	@Override
	public String addDateDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return addDocumentsNewWithCluster( datasourceName, clientInterface.getDynamicIndexName( indexName),  addTemplate,  beans) ;
	}

	@Override
	public String addDateDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return addDocumentsNewWithCluster( datasourceName, clientInterface.getDynamicIndexName( indexName),  addTemplate,  beans,refreshOption) ;
	}







}
