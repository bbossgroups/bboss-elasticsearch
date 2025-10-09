package org.frameworkset.elasticsearch.client;

import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearch;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.ElasticSearchHelper;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.bulk.BulkCommand;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.entity.sql.ColumnMeta;
import org.frameworkset.elasticsearch.entity.sql.SQLResult;
import org.frameworkset.elasticsearch.entity.suggest.CompleteRestResponse;
import org.frameworkset.elasticsearch.entity.suggest.PhraseRestResponse;
import org.frameworkset.elasticsearch.entity.suggest.TermRestResponse;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.elasticsearch.template.ESInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 提供操作多数据源的elasticsearch client Api
 * @see <p>https://esdoc.bbossgroups.com/#/development</p>
 */
public class RestClientUtil extends AbstractRestClientUtil{
	private static Logger logger = LoggerFactory.getLogger(RestClientUtil.class);

	public RestClientUtil(ElasticSearchClient client, IndexNameBuilder indexNameBuilder) {
		super(client, indexNameBuilder);
	}


	@Override
	public String updateAllIndicesSettingsWithCluster(String datasourceName, Map<String, Object> settings) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateAllIndicesSettings(settings);
	}

	@Override
	public String updateIndiceSettingsWithCluster(String datasourceName, String indice, Map<String, Object> settings) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateIndiceSettings(indice,settings);
	}

	@Override
	public String updateAllIndicesSettingWithCluster(String datasourceName, String key, Object value) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateAllIndicesSetting(key,value);
	}

	@Override
	public String updateIndiceSettingWithCluster(String datasourceName, String indice, String key, Object value) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateIndiceSetting(indice,key,value);
	}

	@Override
	public String updateClusterSettingWithCluster(String datasourceName, ClusterSetting clusterSetting) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateClusterSetting(clusterSetting);
	}

	@Override
	public String updateClusterSettingsWithCluster(String datasourceName, List<ClusterSetting> clusterSettings) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateClusterSettings(clusterSettings);
	}

	@Override
	public String getDynamicIndexNameWithCluster(String datasourceName, String indexName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDynamicIndexName(indexName);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path,entity);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path,templateName,params);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path,templateName,params);
	}

	@Override
	public String addDocumentWithParentIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithParentId(  indexName,   indexType,   bean,   parentId);
	}

	@Override
	public String addDocumentWithParentIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithParentId(indexName, indexType,   bean,   parentId,   refreshOption);
	}

	@Override
	public String addDateDocumentWithParentIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithParentId(indexName,  indexType,  bean,  parentId);
	}

	@Override
	public String addDateDocumentWithParentIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithParentId(indexName,   indexType,   bean,   parentId,   refreshOption);
	}

	@Override
	public String updateByPathWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByPath(     path,   entity);
	}

	@Override
	public String updateByPathWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByPath(path,   templateName,   params);
	}

	@Override
	public String updateByPathWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByPath(path,  templateName,  params);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String type, Object id, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index,  type, id, params);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String type, Object id, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index,  type,  id, params);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String type, Object id, Map params, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index,  type,  id, params, refreshOption);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String type, Object id, Object params, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index,  type,  id, params,  refreshOption);
	}

	@Override
	public String deleteByPathWithCluster(String datasourceName, String path) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteByPath(  path);
	}

	@Override
	public String deleteByQueryWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteByQuery(path, entity);
	}

	@Override
	public String deleteByQueryWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteByQuery(path, templateName, params);
	}

	@Override
	public String deleteByQueryWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteByQuery(path,  templateName,  params);
	}

	@Override
	public String deleteDocumentsWithCluster(String datasourceName, String indexName, String indexType, String[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocuments(indexName, indexType, ids);
	}

	@Override
	public String deleteDocumentsWithrefreshOptionWithCluster(String datasourceName, String indexName, String indexType, String refreshOption, String[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocumentsWithrefreshOption(indexName, indexType, refreshOption, ids);
	}

	@Override
	public <T> T getIndexMappingWithCluster(String datasourceName, String index, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexMapping(index, responseHandler);
	}

	@Override
	public <T> T getIndexMappingWithCluster(String datasourceName, String index, boolean pretty, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexMapping(index, pretty, responseHandler);
	}

	@Override
	public boolean existIndiceWithCluster(String datasourceName, String indiceName) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.existIndice( indiceName);
	}

	@Override
	public boolean existIndiceTypeWithCluster(String datasourceName, String indiceName, String type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.existIndiceType( indiceName, type);
	}

	@Override
	public List<IndexField> getIndexMappingFieldsWithCluster(String datasourceName, String index, String indexType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexMappingFields(index, indexType);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType, addTemplate, beans, refreshOption);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType, addTemplate,  beans);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, addTemplate, bean);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, addTemplate, bean, refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String updateTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType, updateTemplate, beans);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType, updateTemplate, beans, refreshOption);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType, beans, refreshOption);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType, beans);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, bean);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument( bean);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, Object bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument( bean, clientOptions);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, Object params, ClientOptions updateOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(   params,   updateOptions);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, Object documentId, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument( documentId, params);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(  beans);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, List<?> beans, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments( beans,  clientOptions);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, List<?> beans, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments( beans,  clientOptions);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments( beans);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, bean, clientOptions);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, bean, clientOptions);
	}

	@Override
	public String addMapDocumentWithCluster(String datasourceName, String indexName, String indexType, Map bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addMapDocument(indexName, indexType,  bean,  clientOptions);
	}

	@Override
	public String addDateMapDocumentWithCluster(String datasourceName, String indexName, String indexType, Map bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateMapDocument(indexName, indexType, bean);
	}

	@Override
	public String addMapDocumentWithCluster(String datasourceName, String indexName, String indexType, Map bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addMapDocument(indexName, indexType, bean);
	}

	@Override
	public String addDateMapDocumentWithCluster(String datasourceName, String indexName, String indexType, Map bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateMapDocument(indexName, indexType,  bean,  clientOptions);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, bean, refreshOption);
	}

	@Override
	public String addDocumentWithIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithId(indexName, indexType, bean,  docId);
	}

	@Override
	public String addDocumentWithIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithId(indexName, indexType, bean, docId, parentId);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, bean, docId, parentId, refreshOption);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, indexType, bean, docId, refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType, beans, clientOptions);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType, beans);
	}

	@Override
	public String updateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocumentsWithIdKey(indexName, indexType,  beans,  docIdKey);
	}

	@Override
	public String updateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocumentsWithIdKey(indexName, indexType,  beans,  docIdKey,  parentIdKey);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType, beans, refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType,  beans, docIdKey, refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, indexType,  beans,  docIdKey, parentIdKey, refreshOption);
	}

	@Override
	public String getDocumentWithCluster(String datasourceName, String indexName, String indexType, String documentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, indexType, documentId);
	}

	@Override
	public String getDocumentByFieldWithCluster(String datasourceName, String indexName, String fieldName, Object value) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByField(indexName, fieldName, value);
	}

	@Override
	public String getDocumentByFieldWithCluster(String datasourceName, String indexName, String fieldName, Object value, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByField(indexName, fieldName, value, options);
	}

	@Override
	public <T> T getDocumentByFieldWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByField(indexName, fieldName, value, type);
	}

	@Override
	public <T> T getDocumentByFieldWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByField(indexName, fieldName, value, type, options);
	}

	@Override
	public <T> T getDocumentByFieldLikeWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByFieldLike(indexName, fieldName, value, type);
	}

	@Override
	public <T> T getDocumentByFieldLikeWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByFieldLike(indexName, fieldName, value, type, options);
	}

	@Override
	public <T> ESDatas<T> searchListByFieldWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type, int from, int size) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchListByField(indexName, fieldName, value, type, from, size);
	}

	@Override
	public <T> ESDatas<T> searchListByFieldWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type, int from, int size, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchListByField(indexName, fieldName,
				value, type, from, size, options);
	}

	@Override
	public <T> ESDatas<T> searchListByFieldLikeWithCluster(String datasourceName, String indexName, String fieldName, Object value, Class<T> type, int from, int size) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchListByFieldLike(indexName, fieldName, value, type, from, size);
	}

	@Override
	public <T> ESDatas<T> searchListByFieldLikeWithCluster(String datasourceName, String indexName, String fieldName,
														   Object value, Class<T> type, int from, int size, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchListByFieldLike(indexName, fieldName,
				  value, type,  from,  size, options);
	}

	@Override
	public String getDocumentByFieldLikeWithCluster(String datasourceName, String indexName, String fieldName, Object value) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByFieldLike(indexName, fieldName, value);
	}

	@Override
	public String getDocumentByFieldLikeWithCluster(String datasourceName, String indexName, String fieldName, Object value, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByFieldLike(indexName, fieldName, value, options);
	}

	@Override
	public String getDocumentWithCluster(String datasourceName, String indexName, String indexType, String documentId, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, indexType, documentId, options);
	}

	@Override
	public String getDocumentByPathWithCluster(String datasourceName, String path) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByPath(  path);
	}

	@Override
	public String getDocumentSourceWithCluster(String datasourceName, String path) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentSource(  path);
	}

	@Override
	public <T> T getDocumentByPathWithCluster(String datasourceName, String path, Class<T> beanType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentByPath(path, beanType);
	}

	@Override
	public <T> T getDocumentSourceWithCluster(String datasourceName, String path, Class<T> beanType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentSource(path, beanType);
	}

	@Override
	public <T> T getDocumentWithCluster(String datasourceName, String indexName, String indexType, String documentId, Class<T> beanType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, indexType, documentId, beanType);
	}

	@Override
	public <T> T getDocumentWithCluster(String datasourceName, String indexName, String indexType, String documentId, Map<String, Object> options, Class<T> beanType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, indexType, documentId, options, beanType);
	}

	@Override
	public MapSearchHit getDocumentHitWithCluster(String datasourceName, String indexName, String indexType, String documentId, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentHit(indexName, indexType, documentId,  options);
	}

	@Override
	public MapSearchHit getDocumentHitWithCluster(String datasourceName, String indexName, String indexType, String documentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentHit(indexName, indexType, documentId);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, bean);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, bean, refreshOption);
	}

	@Override
	public String addDateDocumentWithIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithId(indexName, indexType, bean, docId);
	}

	@Override
	public String addDateDocumentWithIdWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithId(indexName, indexType, bean, docId, parentId);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, bean, docId, refreshOption);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, bean, docId, parentId, refreshOption);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType, beans);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType, beans, refreshOption);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType,  beans, docIdKey, refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdKey(indexName, indexType,  beans,  docIdKey);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType,  beans, docIdKey, refreshOption);
	}

	@Override
	public String addDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans, String docIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdKey(indexName, indexType,  beans,  docIdKey);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<Map> beans,
											  String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType,  beans,
				 docIdKey,  parentIdKey,  refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, String indexType,
													   List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdKey(indexName, indexType,
				beans,  docIdKey,  parentIdKey);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType,
										  List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType,
				 beans,  docIdKey,  parentIdKey,  refreshOption);
	}

	@Override
	public String addDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, String indexType,
												   List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdKey(indexName, indexType,
				beans,  docIdKey,  parentIdKey);
	}

	@Override
	public String addDateDocumentsWithIdOptionsWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans,
														   String docIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdOptions(indexName, indexType, beans,
				  docIdField, refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans, String docIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdField(indexName, indexType, beans, docIdField);
	}

	@Override
	public String addDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdField(indexName,
				indexType,  beans,  docIdField,  refreshOption);
	}

	@Override
	public String addDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans, String docIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdField(indexName, indexType, beans, docIdField);
	}

	@Override
	public String addDateDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans, String docIdField,
														 String parentIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdField(indexName, indexType, beans, docIdField,
				  parentIdField,   refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans,
														 String docIdField, String parentIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdField(indexName, indexType, beans,
				  docIdField,   parentIdField);
	}

	@Override
	public String addDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans,
													 String docIdField, String parentIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdField(indexName, indexType, beans,
				  docIdField,   parentIdField,   refreshOption);
	}

	@Override
	public String addDocumentsWithIdParentFieldWithCluster(String datasourceName, String indexName, String indexType, List<Object> beans,
														   String docIdField, String parentIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdParentField(indexName, indexType, beans,
				  docIdField,   parentIdField);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType, beans,  ClientOptions);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, String indexType, List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, indexType, beans,  ClientOptions);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, addTemplate, bean);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName,  String indexType,  String addTemplate,Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, indexType, addTemplate, bean, refreshOption);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType, addTemplate,  beans);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, String indexType, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, indexType, addTemplate, beans, refreshOption);
	}

	@Override
	public String deleteDocumentWithCluster(String datasourceName, String indexName, String indexType, String id) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocument(indexName, indexType, id);
	}

	@Override
	public String deleteDocumentWithCluster(String datasourceName, String indexName, String indexType, String id, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocument(indexName, indexType, id, refreshOption);
	}

	@Override
	public String executeRequestWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(path, entity);
	}

	@Override
	public String executeRequestWithCluster(String datasourceName, String path) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(  path);
	}

	@Override
	public String executeHttpWithCluster(String datasourceName, String path, String action) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, action);
	}

	@Override
	public <T> T executeHttpWithCluster(String datasourceName, String path, String action, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, action, responseHandler);
	}

	@Override
	public <T> T discoverWithCluster(String datasourceName, String path, String action, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.discover(path, action, responseHandler);
	}

	@Override
	public <T> T executeHttpWithCluster(String datasourceName, String path, String entity, String action, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, entity, action, responseHandler);
	}

	@Override
	public String executeHttpWithCluster(String datasourceName, String path, String entity, String action) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, entity, action);
	}

	@Override
	public <T> T executeHttpWithCluster(String datasourceName, String path, String entity, String action, Map params, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, entity, action, params, responseHandler);
	}

	@Override
	public String executeHttpWithCluster(String datasourceName, String path, String entity, Map params, String action) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, entity, params, action);
	}

	@Override
	public <T> T executeHttpWithCluster(String datasourceName, String path, String entity, String action, Object bean, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, entity, action, bean, responseHandler);
	}

	@Override
	public String executeHttpWithCluster(String datasourceName, String path, String entity, Object bean, String action) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeHttp(path, entity, bean, action);
	}

	@Override
	public String getIndexMappingWithCluster(String datasourceName, String index) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexMapping(index);
	}

	@Override
	public String getIndexMappingWithCluster(String datasourceName, String index, boolean pretty) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexMapping(index, pretty);
	}

	@Override
	public String executeRequestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(path, templateName, params);
	}

	@Override
	public String executeRequestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(path,  templateName,  params);
	}

	@Override
	public <T> T executeRequestWithCluster(String datasourceName, String path, String entity, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(path, entity,  responseHandler);
	}

	@Override
	public <T> T executeRequestWithCluster(String datasourceName, String path, String templateName, Map params, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(path, templateName, params, responseHandler);
	}

	@Override
	public <T> T executeRequestWithCluster(String datasourceName, String path, String templateName, Object params, HttpClientResponseHandler<T> responseHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeRequest(path,  templateName,  params, responseHandler);
	}

	@Override
	public MapRestResponse searchWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path, templateName, params);
	}

	@Override
	public MapRestResponse searchWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path,  templateName,  params);
	}

	@Override
	public MapRestResponse searchWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path, entity);
	}

	@Override
	public TermRestResponse termSuggestWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.termSuggest(path, entity);
	}

	@Override
	public PhraseRestResponse phraseSuggestWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.phraseSuggest(path, entity);
	}

	@Override
	public TermRestResponse termSuggestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.termSuggest(path,  templateName,  params);
	}

	@Override
	public PhraseRestResponse phraseSuggestWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.phraseSuggest(path,  templateName,  params);
	}

	@Override
	public TermRestResponse termSuggestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.termSuggest(path, templateName, params);
	}

	@Override
	public PhraseRestResponse phraseSuggestWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.phraseSuggest(path, templateName, params);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String entity, Class<?> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path, entity, type);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path,  templateName, params, type);
	}

	@Override
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName, String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.complateSuggest(path, templateName, params, type);
	}

	@Override
	public Map<String, Object> searchMapWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchMap(path, templateName, params);
	}

	@Override
	public Map<String, Object> searchMapWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchMap(path,  templateName,  params);
	}

	@Override
	public Map<String, Object> searchMapWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchMap(path, entity);
	}

	@Override
	public String getIndiceWithCluster(String datasourceName, String index) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndice(index);
	}

	@Override
	public String dropIndiceWithCluster(String datasourceName, String index) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.dropIndice(index);
	}

	@Override
	public String updateIndiceMappingWithCluster(String datasourceName, String action, String indexMapping) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateIndiceMapping( action, indexMapping);
	}

	@Override
	public String createIndiceMappingWithCluster(String datasourceName, String indexName, String indexMapping) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createIndiceMapping(indexName, indexMapping);
	}

	@Override
	public String updateIndiceMappingWithCluster(String datasourceName, String action, String templateName, Object parameter) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateIndiceMapping( action, templateName, parameter);
	}

	@Override
	public String createIndiceMappingWithCluster(String datasourceName, String indexName, String templateName, Object parameter) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createIndiceMapping(indexName, templateName, parameter);
	}

	@Override
	public String updateIndiceMappingWithCluster(String datasourceName, String action, String templateName, Map parameter) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateIndiceMapping( action, templateName, parameter);
	}

	@Override
	public String createIndiceMappingWithCluster(String datasourceName, String indexName, String templateName, Map parameter) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createIndiceMapping(indexName, templateName, parameter);
	}

	@Override
	public List<ESIndice> getIndexesWithCluster(String datasourceName) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexes();
	}

	@Override
	public List<ESIndice> getIndexesWithCluster(String datasourceName, String indicePattern) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexes( indicePattern);
	}

	@Override
	public String refreshIndexIntervalWithCluster(String datasourceName, String indexName, int interval) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.refreshIndexInterval(indexName, interval);
	}

	@Override
	public String refreshIndexIntervalWithCluster(String datasourceName, String indexName, String indexType, int interval) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.refreshIndexInterval(indexName, indexType, interval);
	}

	@Override
	public String refreshIndexIntervalWithCluster(String datasourceName, int interval, boolean preserveExisting) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.refreshIndexInterval( interval, preserveExisting);
	}

	@Override
	public String refreshIndexIntervalWithCluster(String datasourceName, int interval) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.refreshIndexInterval( interval);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path, templateName, params, type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path,  templateName, params, type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String entity, Class<?> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path, entity, type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path, templateName, params, type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path,  templateName,  params, type);
	}

	@Override
	public RestResponse searchWithCluster(String datasourceName, String path, String entity, ESTypeReferences type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.search(path, entity, type);
	}

	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchList(path, templateName, params, type);
	}

	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchList(path, type);
	}

	@Override
	public <T> ESDatas<T> searchAllWithCluster(String datasourceName, String index, int fetchSize, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAll(index, fetchSize,type);
	}

	@Override
	public <T> ESDatas<T> searchAllWithCluster(String datasourceName, String index, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAll(index, type);
	}

	@Override
	public <T> ESDatas<T> searchAllWithCluster(String datasourceName, String index, int fetchSize, ScrollHandler<T> scrollHandler, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAll(index, fetchSize, scrollHandler, type);
	}

	@Override
	public <T> ESDatas<T> searchAllWithCluster(String datasourceName, String index, ScrollHandler<T> scrollHandler, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAll(index, scrollHandler,type);
	}

	@Override
	public <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName, String index, int fetchSize, Class<T> type, int thread) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAllParallel(index, fetchSize,type, thread);
	}

	@Override
	public <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName, String index, Class<T> type, int thread) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAllParallel(index, type, thread);
	}

	@Override
	public <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName, String index, int fetchSize, ScrollHandler<T> scrollHandler, Class<T> type, int thread) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAllParallel(index, fetchSize, scrollHandler, type, thread);
	}

	@Override
	public <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName, String index, ScrollHandler<T> scrollHandler, Class<T> type, int thread) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAllParallel(index, scrollHandler,type, thread);
	}

	@Override
	public <T> ESDatas<T> searchScrollWithCluster(String datasourceName, String scroll, String scrollId, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchScroll( scroll, scrollId, type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String dslTemplate, String scroll,
											Object params, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scroll(path, dslTemplate, scroll,
				 params,  type,  scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName, String path, String dslTemplate, String scroll,
													Object params, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollParallel(path, dslTemplate, scroll,
				params, type, scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String dslTemplate, String scroll, Object params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scroll(path, dslTemplate, scroll, params, type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String dslTemplate, String scroll, Map params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scroll(path, dslTemplate, scroll, params, type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String dslTemplate,
											String scroll, Map params, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scroll(path, dslTemplate,
				scroll, params, type, scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName, String path, String dslTemplate, String scroll, Map params,
													Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollParallel(path, dslTemplate, scroll, params,
				type, scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollSliceWithCluster(String datasourceName, String path, String dslTemplate, Map params,
												 String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollSlice(path, dslTemplate, params,
				 scroll, type, scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollSliceParallelWithCluster(String datasourceName, String path, String dslTemplate, Map params,
														 String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollSliceParallel(path, dslTemplate, params,
				 scroll, type, scrollHandler);
	}

	@Override
	public <T> ESDatas<T> scrollSliceWithCluster(String datasourceName, String path, String dslTemplate, Map params, String scroll, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollSlice(path, dslTemplate, params, scroll,type);
	}

	@Override
	public <T> ESDatas<T> scrollSliceParallelWithCluster(String datasourceName, String path, String dslTemplate, Map params, String scroll, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollSliceParallel(path, dslTemplate, params, scroll, type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String entity, String scroll, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scroll(path, entity, scroll, type);
	}

	@Override
	public <T> ESDatas<T> scrollWithCluster(String datasourceName, String path, String entity, String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scroll(path, entity, scroll, type, scrollHandler);
	}

	@Override
	public String searchScrollWithCluster(String datasourceName, String scroll, String scrollId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchScroll(  scroll,  scrollId);
	}

	@Override
	public String deleteScrollsWithCluster(String datasourceName, String[] scrollIds) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteScrolls(  scrollIds);
	}

	@Override
	public String deleteAllScrollsWithCluster(String datasourceName) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteAllScrolls();
	}

	@Override
	public String deleteScrollsWithCluster(String datasourceName, Set<String> scrollIds) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteScrolls( scrollIds);
	}

	@Override
	public String deleteScrollsWithCluster(String datasourceName, List<String> scrollIds) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteScrolls( scrollIds);
	}

	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchList(path,  templateName,  params, type);
	}

	@Override
	public <T> ESDatas<T> searchListWithCluster(String datasourceName, String path, String entity, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchList(path, entity, type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchObject(path, templateName, params, type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchObject(path,  templateName,  params, type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, String entity, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchObject(path, entity, type);
	}

	@Override
	public <T> T searchObjectWithCluster(String datasourceName, String path, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchObject(path, type);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Map params,
																 Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params,
				type,  aggs,  stats, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Object params,
																 Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params,
				type, aggs,  stats,  aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity,
																 Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity,
				 type,  aggs,  stats,  aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params, type, aggs, stats);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Object params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params, type,  aggs,  stats);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, type, aggs, stats);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Map params,
																 Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params,
				type, aggs, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Object params,
																 Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params,
				 type,  aggs, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Class<T> type, 
																 String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, type,
				 aggs, aggBucketHandle);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Map params, Class<T> type, String aggs) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params, type, aggs);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Object params, Class<T> type, String aggs) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, params, type, aggs);
	}

	@Override
	public <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName, String path, String entity, Class<T> type, String aggs) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.searchAgg(path, entity, type, aggs);
	}

	@Override
	public String createTempateWithCluster(String datasourceName, String template, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createTempate( template, entity);
	}

	@Override
	public String createTempateWithCluster(String datasourceName, String template, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createTempate( template, templateName, params);
	}

	@Override
	public String deleteTempateWithCluster(String datasourceName, String template) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteTempate( template);
	}

	@Override
	public String createTempateWithCluster(String datasourceName, String template, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createTempate( template, templateName, params);
	}

	@Override
	public String getTempateWithCluster(String datasourceName, String template) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getTempate(  template);
	}

	@Override
	public String getTempateWithCluster(String datasourceName) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getTempate();
	}

	@Override
	public String cleanAllXPackIndicesWithCluster(String datasourceName) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.cleanAllXPackIndices();
	}

	@Override
	public String updateByQueryWithCluster(String datasourceName, String path) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByQuery(  path);
	}

	@Override
	public String updateByQueryWithCluster(String datasourceName, String path, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByQuery(path, entity);
	}

	@Override
	public String updateByQueryWithCluster(String datasourceName, String path, String templateName, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByQuery(path, templateName, params);
	}

	@Override
	public String updateByQueryWithCluster(String datasourceName, String path, String templateName, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateByQuery(path,  templateName,  params);
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String index, String indexType, Class<T> type, Object[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(index, indexType, type, ids);
	}

	@Override
	public String mgetDocumentsWithCluster(String datasourceName, String index, String indexType, Object[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(index, indexType,  ids);
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String path, String entity, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(path, entity, type);
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(path,  templateName,  params, type);
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(path, templateName, params, type);
	}

	@Override
	public long countWithCluster(String datasourceName, String index, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.count(index, entity);
	}

	@Override
	public long countWithCluster(String datasourceName, String index, String template, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.count(index, template, params);
	}

	@Override
	public long countWithCluster(String datasourceName, String index, String template, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.count(index, template, params) ;
	}

	@Override
	public long countAllWithCluster(String datasourceName, String index) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.countAll(index);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String indexType, Object id, Object params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, indexType, id, params, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String indexType, Object id, Map params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, indexType, id, params, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String indexType, Object id,
											Map params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, indexType, id,
				params, refreshOption, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String indexType, Object id,
											Object params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, indexType, id,
				params, refreshOption, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, String indexType, Object params, ClientOptions updateOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, indexType, params, updateOptions);
	}

	@Override
	public String reindexWithCluster(String datasourceName, String sourceIndice, String destIndice) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.reindex(  sourceIndice,  destIndice);
	}

	@Override
	public String reindexByDslWithCluster(String datasourceName, String actionUrl, String dslName, Object params) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.reindexByDsl(  actionUrl,  dslName,  params);
	}

	@Override
	public String reindexByDslWithCluster(String datasourceName, String actionUrl, String dsl) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.reindexByDsl(  actionUrl,  dsl);
	}

	@Override
	public String reindexWithCluster(String datasourceName, String sourceIndice, String destIndice, String versionType) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.reindex( sourceIndice,  destIndice,  versionType);
	}

	@Override
	public String reindexWithCluster(String datasourceName, String sourceIndice, String destIndice, String opType, String conflicts) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.reindex( sourceIndice,  destIndice,  opType,  conflicts);
	}

	@Override
	public String addAliasWithCluster(String datasourceName, String indice, String alias) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addAlias(  indice,  alias);
	}

	@Override
	public String removeAliasWithCluster(String datasourceName, String indice, String alias) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.removeAlias(  indice,  alias);
	}

	@Override
	public <T> SQLResult<T> fetchQueryWithCluster(String datasourceName, Class<T> beanType, String entity, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.fetchQuery( beanType,  entity,  params);
	}

	@Override
	public <T> SQLResult<T> fetchQueryWithCluster(String datasourceName, Class<T> beanType, String entity, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.fetchQuery( beanType,  entity,  bean);
	}

	@Override
	public <T> SQLResult<T> fetchQueryWithCluster(String datasourceName, Class<T> beanType, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.fetchQuery( beanType,  entity);
	}

	@Override
	public <T> SQLResult<T> fetchQueryByCursorWithCluster(String datasourceName, Class<T> beanType, String cursor, ColumnMeta[] metas) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.fetchQueryByCursor( beanType,  cursor, metas);
	}

	@Override
	public <T> SQLResult<T> fetchQueryByCursorWithCluster(String datasourceName, Class<T> beanType, SQLResult<T> oldPage) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.fetchQueryByCursor( beanType, oldPage);
	}

	@Override
	public <T> List<T> sqlWithCluster(String datasourceName, Class<T> beanType, String entity, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.sql( beanType,  entity, params);
	}

	@Override
	public <T> List<T> sqlWithCluster(String datasourceName, Class<T> beanType, String entity, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.sql( beanType,  entity, bean);
	}

	@Override
	public <T> List<T> sqlWithCluster(String datasourceName, Class<T> beanType, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.sql( beanType,  entity);
	}

	@Override
	public <T> T sqlObjectWithCluster(String datasourceName, Class<T> beanType, String entity, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.sqlObject( beanType,  entity,  params);
	}

	@Override
	public <T> T sqlObjectWithCluster(String datasourceName, Class<T> beanType, String entity, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.sqlObject( beanType,  entity,  bean);
	}

	@Override
	public <T> T sqlObjectWithCluster(String datasourceName, Class<T> beanType, String entity) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.sqlObject( beanType,  entity);
	}

	@Override
	public String closeSQLCursorWithCluster(String datasourceName, String cursor) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.closeSQLCursor(  cursor);
	}

	@Override
	public ESInfo getESInfoWithCluster(String datasourceName, String templateName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getESInfo(  templateName);
	}

	@Override
	public String closeIndexWithCluster(String datasourceName, String index) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.closeIndex(index);
	}

	@Override
	public String openIndexWithCluster(String datasourceName, String index) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.openIndex(index);
	}

	@Override
	public String getClusterSettingsWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getClusterSettings();
	}

	@Override
	public String getClusterSettingsWithCluster(String datasourceName, boolean includeDefault) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getClusterSettings(  includeDefault);
	}

	@Override
	public String getIndiceSettingWithCluster(String datasourceName, String indice, String params) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndiceSetting( indice, params);
	}

	@Override
	public String getIndiceSettingWithCluster(String datasourceName, String indice) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndiceSetting( indice);
	}

	@Override
	public String getIndiceSettingByNameWithCluster(String datasourceName, String indice, String settingName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndiceSettingByName( indice, settingName);
	}

	@Override
	public String unassignedNodeLeftDelayedTimeoutWithCluster(String datasourceName, String delayedTimeout) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.unassignedNodeLeftDelayedTimeout(  delayedTimeout);
	}

	@Override
	public String unassignedNodeLeftDelayedTimeoutWithCluster(String datasourceName, String indice, String delayedTimeout) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.unassignedNodeLeftDelayedTimeout( indice, delayedTimeout);
	}

	@Override
	public String updateNumberOfReplicasWithCluster(String datasourceName, String indice, int numberOfReplicas) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateNumberOfReplicas(  indice,  numberOfReplicas);
	}

	@Override
	public String updateNumberOfReplicasWithCluster(String datasourceName, int numberOfReplicas) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateNumberOfReplicas(  numberOfReplicas);
	}

	@Override
	public String disableClusterRoutingAllocationWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.disableClusterRoutingAllocation();
	}

	@Override
	public String enableClusterRoutingAllocationWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.enableClusterRoutingAllocation();
	}

	@Override
	public String flushSyncedWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.flushSynced();
	}

	@Override
	public String flushSyncedWithCluster(String datasourceName, String indice) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.flushSynced(  indice);
	}

	@Override
	public String getCurrentDateStringWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getCurrentDateString();
	}

	@Override
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName, String path, String entity,
													String scroll, Class<T> type, ScrollHandler<T> scrollHandler) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.scrollParallel(path, entity,
				 scroll, type, scrollHandler);
	}

	@Override
	public String forcemergeWithCluster(String datasourceName, String indices) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.forcemerge(  indices);
	}

	@Override
	public String forcemergeWithCluster(String datasourceName, String indices, MergeOption mergeOption) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.forcemerge(  indices,  mergeOption);
	}

	@Override
	public String forcemergeWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.forcemerge();
	}

	@Override
	public String forcemergeWithCluster(String datasourceName, MergeOption mergeOption) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.forcemerge(  mergeOption);
	}

	@Override
	public String createScriptWithCluster(String datasourceName, String scriptName, String scriptDsl) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createScript(  scriptName,  scriptDsl);
	}

	@Override
	public String createScriptWithCluster(String datasourceName, String scriptName, String scriptDslTemplate, Map params) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createScript(   scriptName,   scriptDslTemplate,   params);
	}

	@Override
	public String createScriptWithCluster(String datasourceName, String scriptName, String scriptDslTemplate, Object params) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.createScript(   scriptName,   scriptDslTemplate,   params);
	}

	@Override
	public String deleteScriptWithCluster(String datasourceName, String scriptName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteScript(   scriptName);
	}

	@Override
	public String getScriptWithCluster(String datasourceName, String scriptName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getScript(   scriptName);
	}

	@Override
	public String executeBulkWithCluster(String datasourceName, BulkCommand bulkCommand) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.executeBulk(   bulkCommand);
	}

	@Override
	public String deleteDocumentsWithCluster(String datasourceName, String indexName, String indexType, String[] ids, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocuments(indexName, indexType, ids,  clientOptions);
	}

	@Override
	public Map getClusterInfoWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getClusterInfo();
	}

	@Override
	public String getElasticsearchVersionWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getElasticsearchVersion();
	}


	@Override
	public ElasticSearch getElasticSearchWithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getElasticSearch();
	}

	@Override
	public String addDocumentWithParentIdWithCluster(String datasourceName, String indexName, Object bean, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithParentId(indexName, bean, parentId);
	}

	@Override
	public String addDocumentWithParentIdWithCluster(String datasourceName, String indexName, Object bean, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithParentId(indexName, bean, parentId, refreshOption);
	}

	@Override
	public String addDateDocumentWithParentIdWithCluster(String datasourceName, String indexName, Object bean, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithParentId(indexName, bean, parentId);
	}

	@Override
	public String addDateDocumentWithParentIdWithCluster(String datasourceName, String indexName, Object bean, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithParentId(indexName, bean, parentId, refreshOption);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Map params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, id, params);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Object params) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, id, params);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Map params, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, id, params,  refreshOption);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Object params, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, id, params,  refreshOption);
	}

	@Override
	public String deleteDocumentsWithCluster(String datasourceName, String indexName, String[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocuments(indexName, ids);
	}

	@Override
	public String deleteDocumentsWithrefreshOptionWithCluster(String datasourceName, String indexName, String refreshOption, String[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocumentsWithrefreshOption(indexName, refreshOption, ids);
	}

	@Override
	public List<IndexField> getIndexMappingFieldsWithCluster(String datasourceName, String index) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getIndexMappingFields(index);
	}

	@Override
	public String addDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsNew(indexName, addTemplate, beans,  refreshOption);
	}

	@Override
	public String addDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsNew(indexName, addTemplate, beans);
	}

	@Override
	public String addDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentNew(indexName, addTemplate,  bean);
	}

	@Override
	public String addDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentNew(indexName, addTemplate,  bean,  refreshOption);
	}

	@Override
	public String updateDocumentsNewWithCluster(String datasourceName, String indexName, String updateTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocumentsNew(indexName, updateTemplate,  beans);
	}

	@Override
	public String updateDocumentsNewWithCluster(String datasourceName, String indexName, String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocumentsNew(indexName, updateTemplate,beans,  refreshOption);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName,  beans,  refreshOption);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, beans);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, bean);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, Object bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, bean, clientOptions);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, Object bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, bean, clientOptions);
	}

	@Override
	public String addMapDocumentWithCluster(String datasourceName, String indexName, Map bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addMapDocument(indexName, bean, clientOptions);
	}

	@Override
	public String addDateMapDocumentWithCluster(String datasourceName, String indexName, Map bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateMapDocument(indexName, bean);
	}

	@Override
	public String addMapDocumentWithCluster(String datasourceName, String indexName, Map bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addMapDocument(indexName, bean);
	}

	@Override
	public String addDateMapDocumentWithCluster(String datasourceName, String indexName, Map bean, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateMapDocument(indexName, bean, clientOptions);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, bean, refreshOption);
	}

	@Override
	public String addDocumentWithIdWithCluster(String datasourceName, String indexName, Object bean, Object docId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithId(indexName, bean, docId);
	}

	@Override
	public String addDocumentWithIdWithCluster(String datasourceName, String indexName, Object bean, Object docId, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentWithId(indexName, bean, docId, parentId);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, bean, docId, parentId, refreshOption);
	}

	@Override
	public String addDocumentWithCluster(String datasourceName, String indexName, Object bean, Object docId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocument(indexName, bean, docId, refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, List<?> beans, ClientOptions clientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, beans, clientOptions);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, beans);
	}

	@Override
	public String updateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocumentsWithIdKey(indexName, beans, docIdKey);
	}

	@Override
	public String updateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocumentsWithIdKey(indexName, beans, docIdKey, parentIdKey);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, beans, refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, beans, docIdKey,  refreshOption);
	}

	@Override
	public String updateDocumentsWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocuments(indexName, beans, docIdKey,  parentIdKey, refreshOption);
	}

	@Override
	public String getDocumentWithCluster(String datasourceName, String indexName, String documentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, documentId);
	}

	@Override
	public String getDocumentWithCluster(String datasourceName, String indexName, String documentId, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, documentId,  options);
	}

	@Override
	public <T> T getDocumentWithCluster(String datasourceName, String indexName, String documentId, Class<T> beanType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, documentId,beanType);
	}

	@Override
	public <T> T getDocumentWithCluster(String datasourceName, String indexName, String documentId, Map<String, Object> options, Class<T> beanType) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocument(indexName, documentId,  options,  beanType);
	}

	@Override
	public MapSearchHit getDocumentHitWithCluster(String datasourceName, String indexName, String documentId, Map<String, Object> options) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentHit(indexName, documentId,  options);
	}

	@Override
	public MapSearchHit getDocumentHitWithCluster(String datasourceName, String indexName, String documentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.getDocumentHit(indexName, documentId);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, bean);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, bean, refreshOption);
	}

	@Override
	public String addDateDocumentWithIdWithCluster(String datasourceName, String indexName, Object bean, Object docId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithId(indexName, bean, docId);
	}

	@Override
	public String addDateDocumentWithIdWithCluster(String datasourceName, String indexName, Object bean, Object docId, Object parentId) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentWithId(indexName, bean, docId,  parentId);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, Object bean, Object docId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, bean, docId, refreshOption);
	}

	@Override
	public String addDateDocumentWithCluster(String datasourceName, String indexName, Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocument(indexName, bean, docId,  parentId, refreshOption);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName,beans);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName,beans, refreshOption);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, beans, docIdKey,  refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdKey(indexName, beans, docIdKey);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, beans, docIdKey,  refreshOption);
	}

	@Override
	public String addDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdKey(indexName, beans, docIdKey);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName, beans, docIdKey,  parentIdKey, refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdKey(indexName, beans, docIdKey,  parentIdKey);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName, beans, docIdKey,  parentIdKey, refreshOption);
	}

	@Override
	public String addDocumentsWithIdKeyWithCluster(String datasourceName, String indexName, List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdKey(indexName, beans, docIdKey,  parentIdKey);
	}

	@Override
	public String addDateDocumentsWithIdOptionsWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdOptions(indexName,beans, docIdField,  refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdField(indexName,beans, docIdField);
	}

	@Override
	public String addDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdField(indexName,beans, docIdField,  refreshOption);
	}

	@Override
	public String addDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdField(indexName,beans, docIdField);
	}

	@Override
	public String addDateDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField,
														 String parentIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdField(indexName,beans, docIdField,parentIdField,  refreshOption);
	}

	@Override
	public String addDateDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField, String parentIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsWithIdField(indexName,beans, docIdField,  parentIdField);
	}

	@Override
	public String addDocumentsWithIdFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField, String parentIdField, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdField(indexName,beans, docIdField,  parentIdField, refreshOption);
	}

	@Override
	public String addDocumentsWithIdParentFieldWithCluster(String datasourceName, String indexName, List<Object> beans, String docIdField, String parentIdField) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocumentsWithIdParentField(indexName,beans, docIdField,  parentIdField);
	}

	@Override
	public String addDateDocumentsWithCluster(String datasourceName, String indexName, List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocuments(indexName,beans, ClientOptions);
	}

	@Override
	public String addDocumentsWithCluster(String datasourceName, String indexName, List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDocuments(indexName,beans, ClientOptions);
	}

	@Override
	public String addDateDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentNew(indexName, addTemplate,  bean);
	}

	@Override
	public String addDateDocumentNewWithCluster(String datasourceName, String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentNew(indexName, addTemplate,  bean,  refreshOption);
	}

	@Override
	public String addDateDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsNew(indexName, addTemplate, beans);
	}

	@Override
	public String addDateDocumentsNewWithCluster(String datasourceName, String indexName, String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.addDateDocumentsNew(indexName, addTemplate, beans, refreshOption);
	}

	@Override
	public String deleteDocumentNewWithCluster(String datasourceName, String indexName, String id) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocumentNew(indexName, id);
	}

	@Override
	public String deleteDocumentNewWithCluster(String datasourceName, String indexName, String id, String refreshOption) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deleteDocumentNew(indexName, id, refreshOption);
	}

	@Override
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName, String index, Class<T> type, Object... ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(index,  type, ids);
	}

	@Override
	public String mgetDocumentsWithCluster(String datasourceName, String index, Object[] ids) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.mgetDocuments(index, ids);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Object params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, id, params, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Map params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, id, params, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Map params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument( index,  id,  params,  refreshOption,  detect_noop,  doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object id, Object params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument( index,  id, params, refreshOption, detect_noop, doc_as_upsert);
	}

	@Override
	public String updateDocumentWithCluster(String datasourceName, String index, Object params, ClientOptions updateOptions) throws ElasticSearchException {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.updateDocument(index, params, updateOptions);
	}

	@Override
	public boolean isVersionUpper7WithCluster(String datasourceName) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.isVersionUpper7() ;
	}

	@Override
	public PitId requestPitIdWithCluster(String datasourceName, String index, String keepaliveTime) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.requestPitId( index,  keepaliveTime);
	}

	@Override
	public String deletePitIdWithCluster(String datasourceName, String pitId) {
		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil(datasourceName);
		return clientInterface.deletePitId( pitId);
	}
}
