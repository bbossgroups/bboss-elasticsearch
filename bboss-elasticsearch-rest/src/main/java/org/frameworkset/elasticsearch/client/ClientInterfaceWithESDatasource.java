package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2020 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0WithCluster(String datasourceName,the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearch;
import org.frameworkset.elasticsearch.ElasticSearchException;
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
import org.frameworkset.elasticsearch.template.TemplateContainer;
import org.frameworkset.util.annotations.ThreadSafe;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Description: 定义一套完成的带es数据源的api接口</p>
 * <p></p>
 * <p>CopyrightWithCluster(String datasourceName,c) 2020</p>
 * @Date 2021/10/12 11:36
 * @author biaoping.yin
 * @version 1.0
 */
@ThreadSafe
public interface ClientInterfaceWithESDatasource extends ClientInterfaceNew{

	public String updateAllIndicesSettingsWithCluster(String datasourceName,Map<String,Object> settings) ;
	public String updateIndiceSettingsWithCluster(String datasourceName,String indice,Map<String,Object> settings) ;

	public String updateAllIndicesSettingWithCluster(String datasourceName,String key,Object value) ;
	public String updateIndiceSettingWithCluster(String datasourceName,String indice,String key,Object value) ;

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-update-settings.html
	 * Cluster Update Settingsedit
	 * 	 Use this API to review and change cluster-wide settings.
	 *
	 * 	 To review cluster settings:
	 *
	 * 	 GET /_cluster/settings
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 By default, this API call only returns settings that have been explicitly defined, but can also include the default settings.
	 *
	 * 	 Updates to settings can be persistent, meaning they apply across restarts, or transient, where they don’t survive a full cluster restart. Here is an example of a persistent update:
	 *
	 * 	 PUT /_cluster/settings
	 *          {
	 * 	 "persistent" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : "50mb"
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 This update is transient:
	 *
	 * 	 PUT /_cluster/settings?flat_settings=true
	 *     {
	 * 	 "transient" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : "20mb"
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 The response to an update returns the changed setting, as in this response to the transient example:
	 *
	 *     {
	 * 	 ...
	 * 	 "persistent" : { },
	 * 	 "transient" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : "20mb"
	 *     }
	 *     }
	 * 	 You can reset persistent or transient settings by assigning a null value. If a transient setting is reset, the first one of these values that is defined is applied:
	 *
	 * 	 the persistent setting
	 * 	 the setting in the configuration file
	 * 	 the default value.
	 * 	 This example resets a setting:
	 *
	 * 	 PUT /_cluster/settings
	 *     {
	 * 	 "transient" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : null
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 The response does not include settings that have been reset:
	 *
	 *     {
	 * 	 ...
	 * 	 "persistent" : {},
	 * 	 "transient" : {}
	 *     }
	 * 	 You can also reset settings using wildcards. For example, to reset all dynamic indices.recovery settings:
	 *
	 * 	 PUT /_cluster/settings
	 *     {
	 * 	 "transient" : {
	 * 	 "indices.recovery.*" : null
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 Order of Precedenceedit
	 * 	 The order of precedence for cluster settings is:
	 *
	 * 	 transient cluster settings
	 * 	 persistent cluster settings
	 * 	 settings in the elasticsearch.yml configuration file.
	 * 	 It’s best to set all cluster-wide settings with the settings API and use the elasticsearch.yml file only for local configurations. This way you can be sure that the setting is the same on all nodes. If, on the other hand, you define different settings on different nodes by accident using the configuration file, it is very difficult to notice these discrepancies.
	 *
	 * 	 You can find the list of settings that you can dynamically update in Modules.
	 * @param clusterSetting
	 * @return
	 */
	public String updateClusterSettingWithCluster(String datasourceName,ClusterSetting clusterSetting);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-update-settings.html
	 * Cluster Update Settingsedit
	 * 	 Use this API to review and change cluster-wide settings.
	 *
	 * 	 To review cluster settings:
	 *
	 * 	 GET /_cluster/settings
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 By default, this API call only returns settings that have been explicitly defined, but can also include the default settings.
	 *
	 * 	 Updates to settings can be persistent, meaning they apply across restarts, or transient, where they don’t survive a full cluster restart. Here is an example of a persistent update:
	 *
	 * 	 PUT /_cluster/settings
	 *          {
	 * 	 "persistent" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : "50mb"
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 This update is transient:
	 *
	 * 	 PUT /_cluster/settings?flat_settings=true
	 *     {
	 * 	 "transient" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : "20mb"
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 The response to an update returns the changed setting, as in this response to the transient example:
	 *
	 *     {
	 * 	 ...
	 * 	 "persistent" : { },
	 * 	 "transient" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : "20mb"
	 *     }
	 *     }
	 * 	 You can reset persistent or transient settings by assigning a null value. If a transient setting is reset, the first one of these values that is defined is applied:
	 *
	 * 	 the persistent setting
	 * 	 the setting in the configuration file
	 * 	 the default value.
	 * 	 This example resets a setting:
	 *
	 * 	 PUT /_cluster/settings
	 *     {
	 * 	 "transient" : {
	 * 	 "indices.recovery.max_bytes_per_sec" : null
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 The response does not include settings that have been reset:
	 *
	 *     {
	 * 	 ...
	 * 	 "persistent" : {},
	 * 	 "transient" : {}
	 *     }
	 * 	 You can also reset settings using wildcards. For example, to reset all dynamic indices.recovery settings:
	 *
	 * 	 PUT /_cluster/settings
	 *     {
	 * 	 "transient" : {
	 * 	 "indices.recovery.*" : null
	 *     }
	 *     }
	 * 	 COPY AS CURLVIEW IN CONSOLE
	 * 	 Order of Precedenceedit
	 * 	 The order of precedence for cluster settings is:
	 *
	 * 	 transient cluster settings
	 * 	 persistent cluster settings
	 * 	 settings in the elasticsearch.yml configuration file.
	 * 	 It’s best to set all cluster-wide settings with the settings API and use the elasticsearch.yml file only for local configurations. This way you can be sure that the setting is the same on all nodes. If, on the other hand, you define different settings on different nodes by accident using the configuration file, it is very difficult to notice these discrepancies.
	 *
	 * 	 You can find the list of settings that you can dynamically update in Modules.
	 * @param clusterSettings
	 * @return
	 */
	public String updateClusterSettingsWithCluster(String datasourceName,List<ClusterSetting> clusterSettings);

	/**
	 * 获取动态索引表名称
	 * @param indexName
	 * @return
	 */
	public String getDynamicIndexNameWithCluster(String datasourceName,String indexName);
	public CompleteRestResponse complateSuggestWithCluster(String datasourceName,String path, String entity) throws ElasticSearchException;

	public CompleteRestResponse complateSuggestWithCluster(String datasourceName,String path, String templateName,Map params) throws ElasticSearchException;

	public CompleteRestResponse complateSuggestWithCluster(String datasourceName,String path, String templateName,Object params) throws ElasticSearchException;
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocumentWithParentIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object parentId) throws ElasticSearchException;

	public String addDocumentWithParentIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object parentId,String refreshOption) throws ElasticSearchException;

	public String addDateDocumentWithParentIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object parentId) throws ElasticSearchException;

	public String addDateDocumentWithParentIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object parentId,String refreshOption) throws ElasticSearchException;
	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param entity
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPathWithCluster(String datasourceName,String path,String entity) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPathWithCluster(String datasourceName,String path,String templateName,Map params) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPathWithCluster(String datasourceName,String path,String templateName,Object params) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param type
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String type,Object id,Map params) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param type
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String type,Object id,Object params) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param type
	 * @param id
	 * @param params
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String type,Object id,Map params,String refreshOption) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param type
	 * @param id
	 * @param params
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String type,Object id,Object params,String refreshOption) throws ElasticSearchException;
	/**
	 * 删除索引文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html
	 * @param path /twitter/_doc/1
	 *             /twitter/_doc/1?routing=kimchy
	 *             /twitter/_doc/1?timeout=5m
	 * @return
	 */
	public String deleteByPathWithCluster(String datasourceName,String path) throws ElasticSearchException;

	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param entity
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQueryWithCluster(String datasourceName,String path,String entity) throws ElasticSearchException;
	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQueryWithCluster(String datasourceName,String path,String templateName,Map params) throws ElasticSearchException;
	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQueryWithCluster(String datasourceName,String path,String templateName,Object params) throws ElasticSearchException;

	public String deleteDocumentsWithCluster(String datasourceName,String indexName, String indexType, String[] ids) throws ElasticSearchException;
	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.

	 * @param ids
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteDocumentsWithrefreshOptionWithCluster(String datasourceName,String indexName, String indexType, String refreshOption,String[] ids) throws ElasticSearchException;
	public <T> T getIndexMappingWithCluster(String datasourceName,String index, ResponseHandler<T> responseHandler) throws ElasticSearchException;
	public <T> T getIndexMappingWithCluster(String datasourceName,String index,boolean pretty,ResponseHandler<T> responseHandler) throws ElasticSearchException;

	/**
	 * 判断索引是否存在
	 * @param indiceName
	 * @return
	 * @throws ElasticSearchException
	 */
	public boolean existIndiceWithCluster(String datasourceName,String indiceName) throws ElasticSearchException;

	/**
	 * 判断所引类型是否存在
	 * @param indiceName
	 * @param type
	 * @return
	 * @throws ElasticSearchException
	 */
	public boolean existIndiceTypeWithCluster(String datasourceName,String indiceName,String type) throws ElasticSearchException;
	/**
	 * 获取索引表
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public List<IndexField> getIndexMappingFieldsWithCluster(String datasourceName,String index, String indexType) throws ElasticSearchException;

	/***************************读取模板文件添加或者修改文档开始************************************/
	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @param refreshOption
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException;

	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param updateTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType,String updateTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;

	/***************************读取模板文件添加或者修改文档结束************************************/

	/***************************添加或者修改文档开始************************************/
	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType,  List<?> beans) throws ElasticSearchException;


	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 *  indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,Object bean) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 *  indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param bean
	 * @param clientOptions
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,Object bean,ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param params
	 * @param updateOptions
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,Object params,ClientOptions updateOptions) throws ElasticSearchException;

	/**
	 * indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param documentId
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,Object documentId,Object params) throws ElasticSearchException;

	/**
	 * indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,List<?> beans) throws ElasticSearchException;

	/**
	 * indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param beans
	 * @param clientOptions
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;
	/**
	 *
	 * indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param beans
	 * @param clientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName, List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * indexName，	 indexType索引类型和type必须通过bean对象的ESIndex来指定，否则抛出异常
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName, List<?> beans) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,ClientOptions clientOptions) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,ClientOptions clientOptions) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addMapDocumentWithCluster(String datasourceName,String indexName, String indexType, Map bean,ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateMapDocumentWithCluster(String datasourceName,String indexName, String indexType, Map bean) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addMapDocumentWithCluster(String datasourceName,String indexName, String indexType, Map bean) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateMapDocumentWithCluster(String datasourceName,String indexName, String indexType, Map bean,ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param docId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param docId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId,Object parentId) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param docId
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param docId
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId,String refreshOption) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param clientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans,ClientOptions clientOptions) throws ElasticSearchException;
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans) throws ElasticSearchException;
	public abstract String updateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey) throws ElasticSearchException;
	public abstract String updateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey) throws ElasticSearchException;
	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException;
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException;

	/***************************添加或者修改文档结束************************************/
	/**
	 * 获取json格式文档
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentWithCluster(String datasourceName,String indexName, String indexType,String documentId) throws ElasticSearchException;

	/**
	 * 根据属性获取文档json报文
	 * @param indexName
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentByFieldWithCluster(String datasourceName,String indexName, String fieldName,Object value) throws ElasticSearchException;
	public abstract String getDocumentByFieldWithCluster(String datasourceName,String indexName, String fieldName,Object value,Map<String,Object> options) throws ElasticSearchException;

	public <T> T getDocumentByFieldWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type) throws ElasticSearchException;
	/**
	 * 根据属性获取type类型文档对象
	 * @param indexName
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentByFieldWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type,Map<String,Object> options) throws ElasticSearchException;

	public <T> T getDocumentByFieldLikeWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type) throws ElasticSearchException;
	/**
	 * 根据属性获取type类型文档对象
	 * @param indexName
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentByFieldLikeWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type,Map<String,Object> options) throws ElasticSearchException;



	public <T> ESDatas<T> searchListByFieldWithCluster(String datasourceName,String indexName, String fieldName, Object value, Class<T> type, int from, int size) throws ElasticSearchException;
	/**
	 * 根据属性获取type类型文档对象
	 * @param indexName
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchListByFieldWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type,int from,int size,Map<String,Object> options) throws ElasticSearchException;

	public <T> ESDatas<T> searchListByFieldLikeWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type,int from,int size) throws ElasticSearchException;
	/**
	 * 根据属性获取type类型文档对象
	 * @param indexName
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchListByFieldLikeWithCluster(String datasourceName,String indexName, String fieldName,Object value,Class<T> type,int from,int size,Map<String,Object> options) throws ElasticSearchException;


	/**
	 * 根据属性获取文档json报文
	 * @param indexName
	 * @param fieldName
	 * @param value
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentByFieldLikeWithCluster(String datasourceName,String indexName, String fieldName,Object value) throws ElasticSearchException;
	public abstract String getDocumentByFieldLikeWithCluster(String datasourceName,String indexName, String fieldName,Object value,Map<String,Object> options) throws ElasticSearchException;
	/**
	 * 获取json格式文档，通过options设置获取文档的参数
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentWithCluster(String datasourceName,String indexName, String indexType,String documentId,Map<String,Object> options) throws ElasticSearchException;

	/**
	 * 获取json格式文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/0
	 *             twitter/_doc/0?_source=false
	 *             twitter/_doc/0?_source_include=*.id&_source_exclude=entities
	 *             twitter/_doc/0?_source=*.id,retweeted
	 *             twitter/_doc/1?stored_fields=tags,counter
	 *             twitter/_doc/2?routing=user1&stored_fields=tags,counter
	 *             GET twitter/_doc/2?routing=user1
	 *
	 * @param path
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentByPathWithCluster(String datasourceName,String path) throws ElasticSearchException;


	/**
	 * 获取json格式文档,不带索引元数据
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/1/_source 其中1为文档id
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentSourceWithCluster(String datasourceName,String path) throws ElasticSearchException;

	/**
	 * 获取文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/0
	 *             twitter/_doc/0?_source=false
	 *             twitter/_doc/0?_source_include=*.id&_source_exclude=entities
	 *             twitter/_doc/0?_source=*.id,retweeted
	 *             twitter/_doc/1?stored_fields=tags,counter
	 *             twitter/_doc/2?routing=user1&stored_fields=tags,counter
	 *             GET twitter/_doc/2?routing=user1
	 *
	 * @param path
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentByPathWithCluster(String datasourceName,String path,Class<T> beanType) throws ElasticSearchException;


	/**
	 * 获取文档Source对象，不带索引元数据
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html
	 * @param path twitter/_doc/1/_source 其中1为文档id
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentSourceWithCluster(String datasourceName,String path,Class<T> beanType) throws ElasticSearchException;

	/**
	 * 获取文档,返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentWithCluster(String datasourceName,String indexName, String indexType,String documentId,Class<T> beanType) throws ElasticSearchException;

	/**
	 * 获取文档，通过options设置获取文档的参数，返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentWithCluster(String datasourceName,String indexName, String indexType,String documentId,Map<String,Object> options,Class<T> beanType) throws ElasticSearchException;



	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract MapSearchHit getDocumentHitWithCluster(String datasourceName,String indexName, String indexType, String documentId, Map<String,Object> options) throws ElasticSearchException;

	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param indexType
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract MapSearchHit getDocumentHitWithCluster(String datasourceName,String indexName, String indexType,String documentId) throws ElasticSearchException;

	/**************************************创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,String refreshOption) throws ElasticSearchException;

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId) throws ElasticSearchException;

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithIdWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId,Object parentId) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId,String refreshOption) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType, Object bean,Object docId,Object parentId,String refreshOption) throws ElasticSearchException;

	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans) throws ElasticSearchException;



	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans,String refreshOption) throws ElasticSearchException;

	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdKeyWithCluster(String datasourceName,String indexName, String indexType,  List<Map> beans,String docIdKey) throws ElasticSearchException;


	/**********************/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<Map> beans,String docIdKey,String parentIdKey,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdKeyWithCluster(String datasourceName,String indexName, String indexType,  List<Map> beans,String docIdKey,String parentIdKey) throws ElasticSearchException;

	/**
	 * 指定对象集合的文档id字段
	 */
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Field
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdOptionsWithCluster(String datasourceName,String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, String indexType, List<Object> beans,String docIdField) throws ElasticSearchException;
	public abstract String addDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, String indexType, List<Object> beans,String docIdField,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, String indexType,  List<Object> beans,String docIdField) throws ElasticSearchException;


	/**********************/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField) throws ElasticSearchException;
	public abstract String addDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, String indexType, List<Object> beans,String docIdField,String parentIdField,String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdParentFieldWithCluster(String datasourceName,String indexName, String indexType,  List<Object> beans,String docIdField,String parentIdField) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans,ClientOptions ClientOptions) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param beans
	 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, String indexType, List<?> beans,ClientOptions ClientOptions) throws ElasticSearchException;
	/**************************************创建或者修改文档结束**************************************************************/


	/**************************************基于query dsl配置文件脚本创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException;

	/**************************************基于query dsl配置文件脚本创建或者修改文档结束**************************************************************/

	public abstract String deleteDocumentWithCluster(String datasourceName,String indexName, String indexType, String id) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param indexType
	 * @param id
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteDocumentWithCluster(String datasourceName,String indexName, String indexType, String id,String refreshOption) throws ElasticSearchException;



	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract Object executeRequestWithCluster(String datasourceName,String path, String entity) throws ElasticSearchException;


	/**
	 * @param path
	 * @return
	 */
	public abstract Object executeRequestWithCluster(String datasourceName,String path) throws ElasticSearchException;

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param action
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String executeHttpWithCluster(String datasourceName,String path, String action) throws ElasticSearchException;

	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T executeHttpWithCluster(String datasourceName,String path, String action,ResponseHandler<T> responseHandler) throws ElasticSearchException ;

	public abstract <T> T discoverWithCluster(String datasourceName,String path, String action,ResponseHandler<T> responseHandler) throws ElasticSearchException ;

	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param entity
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  executeHttpWithCluster(String datasourceName,String path, String entity,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException ;

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param entity 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String executeHttpWithCluster(String datasourceName,String path, String entity, String action) throws ElasticSearchException;

	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param entity
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  executeHttpWithCluster(String datasourceName,String path, String entity,String action,Map params,ResponseHandler<T> responseHandler) throws ElasticSearchException ;



	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param entity 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String executeHttpWithCluster(String datasourceName,String path, String entity,Map params, String action) throws ElasticSearchException;
	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param entity
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  executeHttpWithCluster(String datasourceName,String path, String entity,String action,Object bean,ResponseHandler<T> responseHandler) throws ElasticSearchException ;

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param entity 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String executeHttpWithCluster(String datasourceName,String path, String entity,Object bean, String action) throws ElasticSearchException;


	public abstract String getIndexMappingWithCluster(String datasourceName,String index) throws ElasticSearchException;

	public abstract String getIndexMappingWithCluster(String datasourceName,String index,boolean pretty) throws ElasticSearchException;

	public abstract String executeRequestWithCluster(String datasourceName,String path, String templateName, Map params) throws ElasticSearchException;


	public abstract String executeRequestWithCluster(String datasourceName,String path, String templateName, Object params) throws ElasticSearchException;


	public abstract <T> T executeRequestWithCluster(String datasourceName,String path, String entity, ResponseHandler<T> responseHandler) throws ElasticSearchException;

	public abstract <T> T executeRequestWithCluster(String datasourceName,String path, String templateName, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException;


	public abstract <T> T executeRequestWithCluster(String datasourceName,String path, String templateName, Object params, ResponseHandler<T> responseHandler) throws ElasticSearchException;

	public abstract MapRestResponse searchWithCluster(String datasourceName,String path, String templateName, Map params) throws ElasticSearchException;


	public abstract MapRestResponse searchWithCluster(String datasourceName,String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract MapRestResponse searchWithCluster(String datasourceName,String path, String entity) throws ElasticSearchException;
	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract TermRestResponse termSuggestWithCluster(String datasourceName,String path, String entity) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract PhraseRestResponse phraseSuggestWithCluster(String datasourceName,String path, String entity) throws ElasticSearchException;

	/**
	 * @param path
	 * @return
	 */
	public abstract TermRestResponse termSuggestWithCluster(String datasourceName,String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @return
	 */
	public abstract PhraseRestResponse phraseSuggestWithCluster(String datasourceName,String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @return
	 */
	public abstract TermRestResponse termSuggestWithCluster(String datasourceName,String path, String templateName, Map params) throws ElasticSearchException;

	/**
	 * @param path
	 * @return
	 */
	public abstract PhraseRestResponse phraseSuggestWithCluster(String datasourceName,String path, String templateName, Map params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract CompleteRestResponse complateSuggestWithCluster(String datasourceName,String path, String entity,Class<?> type) throws ElasticSearchException;

	/**
	 * @param path
	 * @return
	 */
	public abstract CompleteRestResponse complateSuggestWithCluster(String datasourceName,String path, String templateName, Object params,Class<?> type) throws ElasticSearchException;

	/**
	 * @param path
	 * @return
	 */
	public abstract CompleteRestResponse complateSuggestWithCluster(String datasourceName,String path, String templateName, Map params,Class<?> type) throws ElasticSearchException;

	public abstract Map<String, Object> searchMapWithCluster(String datasourceName,String path, String templateName, Map params) throws ElasticSearchException;


	public abstract Map<String, Object> searchMapWithCluster(String datasourceName,String path, String templateName, Object params) throws ElasticSearchException;

	/**
	 * @param path
	 * @param entity
	 * @return
	 */
	public abstract Map<String, Object> searchMapWithCluster(String datasourceName,String path, String entity) throws ElasticSearchException;


	/**
	 * 获取索引定义
	 *
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getIndiceWithCluster(String datasourceName,String index) throws ElasticSearchException;

	/**
	 * 删除索引定义
	 *
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String dropIndiceWithCluster(String datasourceName,String index) throws ElasticSearchException;

	/**
	 * 更新索引定义：my_index/_mapping
	 * 	  https://www.elastic.co/guide/en/elasticsearch/reference/7.0/indices-put-mapping.html
	 *
	 * @param indexMapping
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateIndiceMappingWithCluster(String datasourceName,String action, String indexMapping) throws ElasticSearchException;

	/**
	 * 创建索引定义
	 * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
	 * {
	 * "settings" : {
	 * "number_of_shards" : 1
	 * },
	 * "mappings" : {
	 * "type1" : {
	 * "properties" : {
	 * "field1" : { "type" : "text" }
	 * }
	 * }
	 * }
	 * }
	 * @param indexName 索引表名称
	 * @param indexMapping 索引mapping dsl名称
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String createIndiceMappingWithCluster(String datasourceName,String indexName, String indexMapping) throws ElasticSearchException;


	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateIndiceMappingWithCluster(String datasourceName,String action, String templateName, Object parameter) throws ElasticSearchException;

	/**
	 * 创建索引定义
	 * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
	 * {
	 * "settings" : {
	 * "number_of_shards" : 1
	 * },
	 * "mappings" : {
	 * "type1" : {
	 * "properties" : {
	 * "field1" : { "type" : "text" }
	 * }
	 * }
	 * }
	 * }
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String createIndiceMappingWithCluster(String datasourceName,String indexName, String templateName, Object parameter) throws ElasticSearchException;

	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateIndiceMappingWithCluster(String datasourceName,String action, String templateName, Map parameter) throws ElasticSearchException;

	/**
	 * 创建索引定义
	 * curl -XPUT 'localhost:9200/test?pretty' -H 'Content-Type: application/json' -d'
	 * {
	 * "settings" : {
	 * "number_of_shards" : 1
	 * },
	 * "mappings" : {
	 * "type1" : {
	 * "properties" : {
	 * "field1" : { "type" : "text" }
	 * }
	 * }
	 * }
	 * }
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String createIndiceMappingWithCluster(String datasourceName,String indexName, String templateName, Map parameter) throws ElasticSearchException;

	/**
	 * 获取elasticsearch索引监控数据，包含以下信息：
	 * health
	 * status
	 * index
	 * uuid
	 * pri
	 * rep
	 * docs.count
	 * docs.deleted
	 * store.size
	 * pri.store.size
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract List<ESIndice> getIndexesWithCluster(String datasourceName) throws ElasticSearchException;
	public List<ESIndice> getIndexesWithCluster(String datasourceName,String indicePattern) throws ElasticSearchException;
	public abstract String refreshIndexIntervalWithCluster(String datasourceName,String indexName, int interval) throws ElasticSearchException;

	public abstract String refreshIndexIntervalWithCluster(String datasourceName,String indexName, String indexType, int interval) throws ElasticSearchException;

	public abstract String refreshIndexIntervalWithCluster(String datasourceName,int interval, boolean preserveExisting) throws ElasticSearchException;

	public abstract String refreshIndexIntervalWithCluster(String datasourceName,int interval) throws ElasticSearchException;

	public abstract RestResponse searchWithCluster(String datasourceName,String path, String templateName, Map params, Class<?> type) throws ElasticSearchException;


	public abstract RestResponse searchWithCluster(String datasourceName,String path, String templateName, Object params, Class<?> type) throws ElasticSearchException;

	public abstract RestResponse searchWithCluster(String datasourceName,String path, String entity, Class<?> type) throws ElasticSearchException;


	public abstract RestResponse searchWithCluster(String datasourceName,String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException;

	public abstract RestResponse searchWithCluster(String datasourceName,String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException;

	public abstract RestResponse searchWithCluster(String datasourceName,String path, String entity, ESTypeReferences type) throws ElasticSearchException;

	public abstract <T> ESDatas<T> searchListWithCluster(String datasourceName,String path, String templateName, Map params, Class<T> type) throws ElasticSearchException;

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html
	 * @param path
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchListWithCluster(String datasourceName,String path,  Class<T> type) throws ElasticSearchException;

	/**
	 * 检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllWithCluster(String datasourceName,String index,  int fetchSize ,Class<T> type) throws ElasticSearchException;

	/**
	 * 检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllWithCluster(String datasourceName,String index,  Class<T> type) throws ElasticSearchException;
	/**
	 * 检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllWithCluster(String datasourceName,String index, int fetchSize , ScrollHandler<T> scrollHandler, Class<T> type) throws ElasticSearchException;

	/**
	 * 检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllWithCluster(String datasourceName,String index,ScrollHandler<T> scrollHandler,  Class<T> type) throws ElasticSearchException;

	/***************************slice searchAll start****************************/

	/**
	 * 并行检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName,String index,  int fetchSize ,Class<T> type,int thread) throws ElasticSearchException;

	/**
	 * 并行检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName,String index,  Class<T> type,int thread) throws ElasticSearchException;
	/**
	 * 并行检索索引所有数据
	 * @param index
	 * @param fetchSize 指定每批次返回的数据，不指定默认为5000
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName,String index,  int fetchSize ,ScrollHandler<T> scrollHandler,Class<T> type,int thread) throws ElasticSearchException;

	/**
	 * 并行检索索引所有数据,每批次返回默认为5000条数据，
	 * @param index
	 * @param scrollHandler 每批数据处理方法
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchAllParallelWithCluster(String datasourceName,String index,ScrollHandler<T> scrollHandler,  Class<T> type,int thread) throws ElasticSearchException;

	/************************************slice searchAll end*****************************/

	/**
	 * scroll search
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-scroll.html
	 * @param scroll
	 * @param scrollId
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> searchScrollWithCluster(String datasourceName,String scroll,String scrollId ,Class<T> type) throws ElasticSearchException;

	/**
	 * scroll检索,每次检索结果交给scrollHandler回调函数处理
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> scrollWithCluster(String datasourceName,String path,String dslTemplate,String scroll,Object params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException;
	public abstract <T> ESDatas<T> scrollParallelWithCluster(String datasourceName,String path,String dslTemplate,String scroll,Object params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException;
	/**
	 * 一次性返回scroll检索结果
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> scrollWithCluster(String datasourceName,String path,String dslTemplate,String scroll,Object params,Class<T> type) throws ElasticSearchException;


	/**
	 * 一次性返回scroll检索结果
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> scrollWithCluster(String datasourceName,String path,String dslTemplate,String scroll,Map params,Class<T> type) throws ElasticSearchException;




	/**
	 * scroll检索,每次检索结果交给scrollHandler回调函数处理
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> scrollWithCluster(String datasourceName,String path,String dslTemplate,String scroll,Map params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException;
	public abstract <T> ESDatas<T> scrollParallelWithCluster(String datasourceName,String path,String dslTemplate,String scroll,Map params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException;


	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain sliceId,sliceMax varialbe
	 * 	 *    <property name="scrollSliceQuery">
	 * 	 *         <![CDATA[
	 * 	 *          {
	 * 	 *            "slice": {
	 * 	 *                 "id": #[sliceId],
	 * 	 *                 "max": #[sliceMax]
	 * 	 *             },
	 * 	 *             "size":#[size],
	 * 	 *             "query": {
	 * 	 *                 "term" : {
	 * 	 *                     "gc.jvmGcOldCount" : 3
	 * 	 *                 }
	 * 	 *             }
	 * 	 *         }
	 * 	 *         ]]>
	 * 	 *     </property>
	 * @param scroll
	 * @param type
	 * @param scrollHandler 每次检索结果会被异步交给handle来处理
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSliceWithCluster(String datasourceName,String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type,
									  ScrollHandler<T> scrollHandler) throws ElasticSearchException;

	public <T> ESDatas<T> scrollSliceParallelWithCluster(String datasourceName,String path,final String dslTemplate,final Map params ,
											  final String scroll  ,final Class<T> type,
											  ScrollHandler<T> scrollHandler) throws ElasticSearchException;
	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain sliceId,sliceMax varialbe
	 *    <property name="scrollSliceQuery">
	 *         <![CDATA[
	 *          {
	 *            "slice": {
	 *                 "id": #[sliceId],
	 *                 "max": #[sliceMax]
	 *             },
	 *             "size":#[size],
	 *             "query": {
	 *                 "term" : {
	 *                     "gc.jvmGcOldCount" : 3
	 *                 }
	 *             }
	 *         }
	 *         ]]>
	 *     </property>
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSliceWithCluster(String datasourceName,String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type) throws ElasticSearchException;

	public <T> ESDatas<T> scrollSliceParallelWithCluster(String datasourceName,String path,final String dslTemplate,final Map params ,
											  final String scroll  ,final Class<T> type) throws ElasticSearchException;
	/**
	 * 一次性返回scroll检索结果
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param entity
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> scrollWithCluster(String datasourceName,String path,String entity,String scroll ,Class<T> type) throws ElasticSearchException;


	/**
	 * scroll检索,每次检索结果列表交给scrollHandler回调函数处理
	 * https://my.oschina.net/bboss/blog/1942562
	 * @param path
	 * @param entity
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> ESDatas<T> scrollWithCluster(String datasourceName,String path,String entity,String scroll ,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException;



	/**
	 * scroll search
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.2/search-request-scroll.html
	 *
	 * @param scroll
	 * @param scrollId

	 * @return
	 * @throws ElasticSearchException
	 */
	public String searchScrollWithCluster(String datasourceName,String scroll,String scrollId ) throws ElasticSearchException;

	/**
	 * 清理scrollId
	 * @param scrollIds
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteScrollsWithCluster(String datasourceName,String [] scrollIds) throws ElasticSearchException;

	/**
	 * 清理all scrollId
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteAllScrollsWithCluster(String datasourceName) throws ElasticSearchException;

	/**
	 * 清理scrollId
	 * @param scrollIds
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteScrollsWithCluster(String datasourceName,Set<String> scrollIds) throws ElasticSearchException;

	public String deleteScrollsWithCluster(String datasourceName,List<String> scrollIds) throws ElasticSearchException;

	public abstract <T> ESDatas<T> searchListWithCluster(String datasourceName,String path, String templateName, Object params, Class<T> type) throws ElasticSearchException;

	public abstract <T> ESDatas<T> searchListWithCluster(String datasourceName,String path, String entity, Class<T> type) throws ElasticSearchException;

	public abstract <T> T searchObjectWithCluster(String datasourceName,String path, String templateName, Map params, Class<T> type) throws ElasticSearchException;

	public abstract <T> T searchObjectWithCluster(String datasourceName,String path, String templateName, Object params, Class<T> type) throws ElasticSearchException;

	public abstract <T> T searchObjectWithCluster(String datasourceName,String path, String entity, Class<T> type) throws ElasticSearchException;

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html
	 * @param path
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T searchObjectWithCluster(String datasourceName,String path,  Class<T> type) throws ElasticSearchException;
	/**
	 * 聚合查询方法
	 *
	 * @param path   es查询请求相对地址
	 * @param entity es聚合查询模板
	 * @param params es聚合查询参数
	 * @param type   es聚合查询结果类
	 * @param aggs   es聚合查询结果名称
	 * @param stats  es聚合查询统计结果名称
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Map params, Class<T> type, String aggs, String stats, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Object params, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Object params, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Class<T> type, String aggs, String stats) throws ElasticSearchException;

	/**
	 * 聚合查询方法
	 *
	 * @param path   es查询请求相对地址
	 * @param entity es聚合查询模板
	 * @param params es聚合查询参数
	 * @param type   es聚合查询结果类
	 * @param aggs   es聚合查询结果名称
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Map params, Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Object params, Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Map params, Class<T> type, String aggs) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Object params, Class<T> type, String aggs) throws ElasticSearchException;

	public abstract <T extends AggHit> ESAggDatas<T> searchAggWithCluster(String datasourceName,String path, String entity, Class<T> type, String aggs) throws ElasticSearchException;

	public abstract String createTempateWithCluster(String datasourceName,String template,String entity)  throws ElasticSearchException;

	public abstract String createTempateWithCluster(String datasourceName,String template, String templateName,Object params) throws ElasticSearchException ;
	/**
	 * 删除模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteTempateWithCluster(String datasourceName,String template) throws ElasticSearchException ;
	public abstract String createTempateWithCluster(String datasourceName,String template, String templateName,Map params) throws ElasticSearchException ;
	/**
	 * 查询模板
	 * @param template
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getTempateWithCluster(String datasourceName,String template) throws ElasticSearchException ;

	/**
	 * 查询所有模板
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getTempateWithCluster(String datasourceName) throws ElasticSearchException ;

	/**
	 * 删除所有xpack相关的监控索引
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String cleanAllXPackIndicesWithCluster(String datasourceName) throws ElasticSearchException;

	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 * @return
	 * @throws ElasticSearchException
	 * @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html#picking-up-a-new-property"
	 */
	public String updateByQueryWithCluster(String datasourceName,String path) throws ElasticSearchException;
	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 *             twitter/_doc/_update_by_query?conflicts=proceed
	 *             twitter/_update_by_query
	 *             twitter,blog/_doc,post/_update_by_query
	 *             twitter/_update_by_query?routing=1
	 *             twitter/_update_by_query?scroll_size=100
	 *             twitter/_update_by_query?pipeline=set-foo
	 *
	 *
	 * @param entity
	 * @return
	 * @throws ElasticSearchException
	 * @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html#picking-up-a-new-property"
	 */
	public String updateByQueryWithCluster(String datasourceName,String path,String entity) throws ElasticSearchException;

	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 *             twitter/_doc/_update_by_query?conflicts=proceed
	 *             twitter/_update_by_query
	 *             twitter,blog/_doc,post/_update_by_query
	 *             twitter/_update_by_query?routing=1
	 *             twitter/_update_by_query?scroll_size=100
	 *             twitter/_update_by_query?pipeline=set-foo
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 * @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html#picking-up-a-new-property"
	 */
	public String updateByQueryWithCluster(String datasourceName,String path,String templateName,Map params) throws ElasticSearchException;

	/**
	 * The simplest usage of _update_by_query just performs an update on every document in the index without changing the source. This is useful to pick up a new property or some other online mapping change. Here is the API:

	 * POST twitter/_update_by_query?conflicts=proceed
	 * @param path twitter/_update_by_query?conflicts=proceed
	 *             twitter/_doc/_update_by_query?conflicts=proceed
	 *             twitter/_update_by_query
	 *             twitter,blog/_doc,post/_update_by_query
	 *             twitter/_update_by_query?routing=1
	 *             twitter/_update_by_query?scroll_size=100
	 *             twitter/_update_by_query?pipeline=set-foo
	 *
	 *
	 * @param templateName
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 * @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update-by-query.html#picking-up-a-new-property"
	 */
	public String updateByQueryWithCluster(String datasourceName,String path,String templateName,Object params) throws ElasticSearchException;

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param index _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param indexType
	 * @param type
	 * @param ids
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName,String index,String indexType,Class<T> type,Object ... ids)  throws ElasticSearchException;
	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param index _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param indexType
	 * @param ids
	 * @return
	 * @throws ElasticSearchException
	 */
	public String mgetDocumentsWithCluster(String datasourceName,String index,String indexType,Object ... ids)  throws ElasticSearchException;
	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param entity
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName,String path,String entity,Class<T> type)  throws ElasticSearchException;

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param templateName
	 * @param params
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName,String path,String templateName,Object params,Class<T> type)  throws ElasticSearchException;

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param templateName
	 * @param params
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName,String path,String templateName,Map params,Class<T> type)  throws ElasticSearchException;
	public long countWithCluster(String datasourceName,String index,String entity)  throws ElasticSearchException;
	public long countWithCluster(String datasourceName,String index,String template,Map params)  throws ElasticSearchException;
	public long countWithCluster(String datasourceName,String index,String template,Object params)  throws ElasticSearchException;

	/**
	 * 查询index中的所有文档数量
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public long countAllWithCluster(String datasourceName,String index) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String indexType,Object id,Object params,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String indexType,Object id,Map params,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String indexType,Object id,Map params,String refreshOption,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param id
	 * @param params
	 * @param refreshOption
	 * @param detect_noop default null
	 * @param doc_as_upsert default null
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String indexType,Object id,Object params,String refreshOption,Boolean detect_noop,Boolean doc_as_upsert) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param indexType
	 * @param params
	 * @param updateOptions 指定更新的相关参数

	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,String indexType,Object params,ClientOptions updateOptions) throws ElasticSearchException;

	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param sourceIndice
	 * @param destIndice
	 * @return
	 */
	public String reindexWithCluster(String datasourceName,String sourceIndice,String destIndice);

	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param actionUrl reindex请求相对url，例如：
	 *                  _reindex
	 *                  _reindex?slices=5&refresh
	 * @param dslName  xml配置文件中dsl对应的name
	 * @param params dslName 中对应的变量参数信息
	 * @return
	 */
	public String reindexByDslWithCluster(String datasourceName,String actionUrl,String dslName,Object params);
	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param actionUrl reindex请求相对url，例如：
	 *                  _reindex
	 *                  _reindex?slices=5&refresh
	 * @param dsl reindex对应的dsl脚本
	 * @return
	 */
	public String reindexByDslWithCluster(String datasourceName,String actionUrl,String dsl);

	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param sourceIndice
	 * @param destIndice
	 * @return
	 */
	public String reindexWithCluster(String datasourceName,String sourceIndice,String destIndice,String versionType);

	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param sourceIndice
	 * @param destIndice
	 * @return
	 */
	public String reindexWithCluster(String datasourceName,String sourceIndice,String destIndice,String opType,String conflicts);
	/**
	 * Associating the alias alias with index indice
	 * more detail see :
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html
	 * @param indice
	 * @param alias
	 * @return
	 */
	public String addAliasWithCluster(String datasourceName,String indice,String alias);

	/**
	 * removing that same alias [alias] of [indice]
	 * more detail see :
	 * 	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-aliases.html
	 * @param indice
	 * @param alias
	 * @return
	 */
	public String removeAliasWithCluster(String datasourceName,String indice,String alias);
	/**
	 * 发送es restful sql 分页查询请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType 返回的对象类型
	 * @param entity dsl
	 * @param params 查询参数
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> SQLResult<T> fetchQueryWithCluster(String datasourceName,Class<T> beanType , String entity , Map params) throws ElasticSearchException ;
	/**
	 * 发送es restful sql 分页查询请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> SQLResult<T>  fetchQueryWithCluster(String datasourceName,Class<T> beanType , String entity ,Object bean) throws ElasticSearchException ;
	/**
	 * 发送es restful sql 分页查询请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> SQLResult<T> fetchQueryWithCluster(String datasourceName,Class<T> beanType, String entity ) throws ElasticSearchException ;
	/**
	 * 发送es restful sql请求下一页数据，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param cursor
	 * @param metas
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T> fetchQueryByCursorWithCluster(String datasourceName,Class<T> beanType, String cursor, ColumnMeta[] metas) throws ElasticSearchException;
	/**
	 * 发送es restful sql请求下一页数据，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param oldPage
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T> fetchQueryByCursorWithCluster(String datasourceName,Class<T> beanType, SQLResult<T> oldPage ) throws ElasticSearchException;

	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> List<T>  sqlWithCluster(String datasourceName,Class<T> beanType , String entity ,Map params) throws ElasticSearchException ;
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> List<T>  sqlWithCluster(String datasourceName,Class<T> beanType , String entity ,Object bean) throws ElasticSearchException ;
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> List<T>  sqlWithCluster(String datasourceName,Class<T> beanType,  String entity ) throws ElasticSearchException ;


	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  sqlObjectWithCluster(String datasourceName,Class<T> beanType , String entity ,Map params) throws ElasticSearchException ;
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  sqlObjectWithCluster(String datasourceName,Class<T> beanType , String entity ,Object bean) throws ElasticSearchException ;
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/sql-rest.html
	 * @param beanType
	 * @param entity

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T  sqlObjectWithCluster(String datasourceName,Class<T> beanType,  String entity ) throws ElasticSearchException ;
	public String closeSQLCursorWithCluster(String datasourceName,String cursor) throws ElasticSearchException ;

	/**
	 * 获取从配置文件定义的dsl元数据
	 * @param templateName
	 * @return
	 */
	public ESInfo getESInfoWithCluster(String datasourceName,String templateName);

	/**
	 * POST /my_index/_close
	 *
	 * POST /my_index/_open
	 */
	public String closeIndexWithCluster(String datasourceName,String index);
	public String openIndexWithCluster(String datasourceName,String index);

	/**
	 * GET /_cluster/settings
	 * @return
	 */
	public String getClusterSettingsWithCluster(String datasourceName);

	public String getClusterSettingsWithCluster(String datasourceName,boolean includeDefault);

	/**
	 * GET indice/_settings
	 * PUT _all/_settings
	 *     {
	 *       "settings": {
	 *         "index.unassigned.node_left.delayed_timeout": "5m"
	 *       }
	 *     }
	 * @param indice
	 * @return
	 */
	public String getIndiceSettingWithCluster(String datasourceName,String indice,String params);
	public String getIndiceSettingWithCluster(String datasourceName,String indice);

	public String getIndiceSettingByNameWithCluster(String datasourceName,String indice,String settingName);

	/**
	 * {
	 *             "settings":{
	 *                 "index.unassigned.node_left.delayed_timeout":"1d"
	 *             }
	 *         }
	 * @param delayedTimeout
	 * @return
	 */
	public String unassignedNodeLeftDelayedTimeoutWithCluster(String datasourceName,String delayedTimeout);
	public String unassignedNodeLeftDelayedTimeoutWithCluster(String datasourceName,String indice,String delayedTimeout);
	public String updateNumberOfReplicasWithCluster(String datasourceName,String indice,int numberOfReplicas);
	public String updateNumberOfReplicasWithCluster(String datasourceName,int numberOfReplicas);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/rolling-upgrades.html
	 * @return
	 */
	public String disableClusterRoutingAllocationWithCluster(String datasourceName);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/rolling-upgrades.html
	 * @return
	 */
	public String enableClusterRoutingAllocationWithCluster(String datasourceName);

	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/indices-synced-flush.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/rolling-upgrades.html
	 * @return
	 */
	public String flushSyncedWithCluster(String datasourceName);
	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/indices-synced-flush.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/6.3/rolling-upgrades.html
	 * @return
	 */
	public String flushSyncedWithCluster(String datasourceName,String indice);

	public String getCurrentDateStringWithCluster(String datasourceName);
	public <T> ESDatas<T> scrollParallelWithCluster(String datasourceName,String path,String entity,String scroll ,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException;

	/**
	 * POST /kimchy,elasticsearch/_forcemerge
	 *
	 * POST /_forcemerge
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html
	 * @param indices
	 * @return
	 */
	public String forcemergeWithCluster(String datasourceName,String indices);

	/**
	 * POST /kimchy,elasticsearch/_forcemerge
	 *
	 * POST /_forcemerge
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html
	 * @param indices
	 * @return
	 */
	public String forcemergeWithCluster(String datasourceName,String indices,MergeOption mergeOption);

	/**
	 * POST /kimchy,elasticsearch/_forcemerge
	 *
	 * POST /_forcemerge
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html
	 * @return
	 */
	public String forcemergeWithCluster(String datasourceName);

	/**
	 * POST /kimchy,elasticsearch/_forcemerge
	 *
	 * POST /_forcemerge
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-forcemerge.html
	 * @return
	 */
	public String forcemergeWithCluster(String datasourceName,MergeOption mergeOption);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 * used by ConfigRestClientUtil and RestClientUtil
	 * @param scriptName
	 * @param scriptDsl
	 * @return
	 */
	public String createScriptWithCluster(String datasourceName,String scriptName,String scriptDsl);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 * used by ConfigRestClientUtil
	 *
	 * @param scriptName
	 * @param scriptDslTemplate
	 * @return
	 */
	public String createScriptWithCluster(String datasourceName,String scriptName,String scriptDslTemplate,Map params);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 * used by ConfigRestClientUtil
	 * @param scriptName
	 * @param scriptDslTemplate
	 * @return
	 */
	public String createScriptWithCluster(String datasourceName,String scriptName,String scriptDslTemplate,Object params);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 * @param scriptName
	 * @return
	 */
	public String deleteScriptWithCluster(String datasourceName,String scriptName);

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 * @param scriptName
	 * @return
	 */
	public String getScriptWithCluster(String datasourceName,String scriptName);

	/**
	 * @see "https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html"
	 * @param bulkCommand
	 * @return
	 */
	String executeBulkWithCluster(String datasourceName,BulkCommand bulkCommand);
	public String deleteDocumentsWithCluster(String datasourceName,String indexName, String indexType, String[] ids,ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * 获取Elasticsearch集群信息,例如：
	 * {
	 *   "name" : "DESKTOP-U3V5C85",
	 *   "cluster_name" : "my-application1",
	 *   "cluster_uuid" : "GZYnO9JCSJqqYUaU9mmRwA",
	 *   "version" : {
	 *     "number" : "7.5.0",
	 *     "build_flavor" : "default",
	 *     "build_type" : "zip",
	 *     "build_hash" : "e9ccaed468e2fac2275a3761849cbee64b39519f",
	 *     "build_date" : "2019-11-26T01:06:52.518245Z",
	 *     "build_snapshot" : false,
	 *     "lucene_version" : "8.3.0",
	 *     "minimum_wire_compatibility_version" : "6.8.0",
	 *     "minimum_index_compatibility_version" : "6.0.0-beta1"
	 *   },
	 *   "tagline" : "You Know, for Search"
	 * }
	 * @return
	 */
	public Map getClusterInfoWithCluster(String datasourceName);

	/**
	 * 获取Elasticsearch 版本号
	 * @return
	 */
	public String getElasticsearchVersionWithCluster(String datasourceName);
	public TemplateContainer getTemplatecontext();



	/**
	 * 获取es数据源参数配置对象
	 * @return
	 */
	public ElasticSearch getElasticSearchWithCluster(String datasourceName) ;


	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocumentWithParentIdWithCluster(String datasourceName,String indexName,   Object bean, Object parentId) throws ElasticSearchException;

	public String addDocumentWithParentIdWithCluster(String datasourceName,String indexName,   Object bean, Object parentId, String refreshOption) throws ElasticSearchException;

	public String addDateDocumentWithParentIdWithCluster(String datasourceName,String indexName, Object bean, Object parentId) throws ElasticSearchException;

	public String addDateDocumentWithParentIdWithCluster(String datasourceName,String indexName,   Object bean, Object parentId, String refreshOption) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_update/1
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,  Object id, Map params) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *            test/_update/1
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index , Object id, Object params) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_update/1
	 * @param id
	 * @param params
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,  Object id, Map params, String refreshOption) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param id
	 * @param params
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,  Object id, Object params, String refreshOption) throws ElasticSearchException;

	/**
	 *
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param ids
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteDocumentsWithCluster(String datasourceName,String indexName,  String[] ids) throws ElasticSearchException;
	/**
	 *
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.

	 * @param ids
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteDocumentsWithrefreshOptionWithCluster(String datasourceName,String indexName,   String refreshOption, String[] ids) throws ElasticSearchException;


	/**
	 * 获取索引表
	 * For Elasticsearch 7 and 7+
	 * @param index
	 * @return
	 * @throws ElasticSearchException
	 */
	public List<IndexField> getIndexMappingFieldsWithCluster(String datasourceName,String index ) throws ElasticSearchException;

	/***************************读取模板文件添加或者修改文档开始************************************/
	/**
	 * 批量创建索引
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param addTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsNewWithCluster(String datasourceName,String indexName,   String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsNewWithCluster(String datasourceName,String indexName,  String addTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentNewWithCluster(String datasourceName,String indexName,  String addTemplate, Object bean) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param addTemplate
	 * @param bean
	 * @param refreshOption
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentNewWithCluster(String datasourceName,String indexName,   String addTemplate, Object bean, String refreshOption) throws ElasticSearchException;

	public abstract String updateDocumentsNewWithCluster(String datasourceName,String indexName,   String updateTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 *
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param updateTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsNewWithCluster(String datasourceName,String indexName , String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException;

	/***************************读取模板文件添加或者修改文档结束************************************/

	/***************************添加或者修改文档开始************************************/
	/**
	 * 批量创建索引
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName,  List<?> beans, String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName,   List<?> beans) throws ElasticSearchException;


	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName,  Object bean) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName,  Object bean, ClientOptions clientOptions) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName,  Object bean, ClientOptions clientOptions) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addMapDocumentWithCluster(String datasourceName,String indexName,  Map bean, ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateMapDocumentWithCluster(String datasourceName,String indexName,  Map bean) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addMapDocumentWithCluster(String datasourceName,String indexName , Map bean) throws ElasticSearchException;
	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateMapDocumentWithCluster(String datasourceName,String indexName,   Map bean, ClientOptions clientOptions) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, Object bean, String refreshOption) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @param docId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithIdWithCluster(String datasourceName,String indexName, Object bean, Object docId) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @param docId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithIdWithCluster(String datasourceName,String indexName,  Object bean, Object docId, Object parentId) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @param docId
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName, Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException;

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param bean
	 * @param docId
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentWithCluster(String datasourceName,String indexName,  Object bean, Object docId, String refreshOption) throws ElasticSearchException;

	/**
	 *
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param beans
	 * @param clientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName,   List<?> beans, ClientOptions clientOptions) throws ElasticSearchException;
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, List<?> beans) throws ElasticSearchException;
	public abstract String updateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey) throws ElasticSearchException;
	public abstract String updateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName,   List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException;
	/**
	 *
	 * @param indexName
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName, List<?> beans, String refreshOption) throws ElasticSearchException;
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName,   List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException;
	public abstract String updateDocumentsWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException;

	/***************************添加或者修改文档结束************************************/
	/**
	 * 获取json格式文档
	 * @param indexName
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentWithCluster(String datasourceName,String indexName,  String documentId) throws ElasticSearchException;
	/**
	 * 获取json格式文档，通过options设置获取文档的参数
	 * @param indexName
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String getDocumentWithCluster(String datasourceName,String indexName,   String documentId, Map<String, Object> options) throws ElasticSearchException;






	/**
	 * 获取文档,返回类型可以继承ESBaseDataWithCluster(String datasourceName,这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
	 * @param indexName
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentWithCluster(String datasourceName,String indexName, String documentId, Class<T> beanType) throws ElasticSearchException;

	/**
	 * 获取文档，通过options设置获取文档的参数，返回类型可以继承ESBaseData(这样方法自动将索引的元数据信息设置到T对象中)和ESId（方法自动将索引文档id设置到对象中）
	 * @param indexName
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract <T> T getDocumentWithCluster(String datasourceName,String indexName,  String documentId, Map<String, Object> options, Class<T> beanType) throws ElasticSearchException;



	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param documentId
	 * @param options
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract MapSearchHit getDocumentHitWithCluster(String datasourceName,String indexName,   String documentId, Map<String, Object> options) throws ElasticSearchException;

	/**
	 * 获取文档MapSearchHit对象，封装了索引文档的所有属性数据
	 * @param indexName
	 * @param documentId
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract MapSearchHit getDocumentHitWithCluster(String datasourceName,String indexName,  String documentId) throws ElasticSearchException;

	/**************************************创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName,   Object bean) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName,  Object bean, String refreshOption) throws ElasticSearchException;

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithIdWithCluster(String datasourceName,String indexName,  Object bean, Object docId) throws ElasticSearchException;

	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithIdWithCluster(String datasourceName,String indexName,   Object bean, Object docId, Object parentId) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName,  Object bean, Object docId, String refreshOption) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentWithCluster(String datasourceName,String indexName,  Object bean, Object docId, Object parentId, String refreshOption) throws ElasticSearchException;

	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName,   List<?> beans) throws ElasticSearchException;



	/**
	 *
	 * @param indexName
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName,  List<?> beans, String refreshOption) throws ElasticSearchException;

	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName,   List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey, String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdKeyWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey) throws ElasticSearchException;


	/**********************/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName,   List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdKey map中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdKeyWithCluster(String datasourceName,String indexName,   List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException;
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey, String parentIdKey, String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdKeyWithCluster(String datasourceName,String indexName,  List<Map> beans, String docIdKey, String parentIdKey) throws ElasticSearchException;

	/**
	 * 指定对象集合的文档id字段
	 */
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdField 对象中作为文档id的Field
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdOptionsWithCluster(String datasourceName,String indexName,   List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdFieldWithCluster(String datasourceName,String indexName, List<Object> beans, String docIdField) throws ElasticSearchException;
	public abstract String addDocumentsWithIdFieldWithCluster(String datasourceName,String indexName,  List<Object> beans, String docIdField, String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdFieldWithCluster(String datasourceName,String indexName,   List<Object> beans, String docIdField) throws ElasticSearchException;


	/**********************/
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdFieldWithCluster(String datasourceName,String indexName,   List<Object> beans, String docIdField, String parentIdField, String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param beans
	 * @param docIdField 对象中作为文档id的Key
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithIdFieldWithCluster(String datasourceName,String indexName , List<Object> beans, String docIdField, String parentIdField) throws ElasticSearchException;
	public abstract String addDocumentsWithIdFieldWithCluster(String datasourceName,String indexName , List<Object> beans, String docIdField, String parentIdField, String refreshOption) throws ElasticSearchException;
	public abstract String addDocumentsWithIdParentFieldWithCluster(String datasourceName,String indexName , List<Object> beans, String docIdField, String parentIdField) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param beans
	 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsWithCluster(String datasourceName,String indexName,   List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param beans
	 * @param ClientOptions 传递es操作的相关控制参数，采用ClientOptions后，定义在对象中的相关注解字段将不会起作用（失效）
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDocumentsWithCluster(String datasourceName,String indexName, List<?> beans, ClientOptions ClientOptions) throws ElasticSearchException;
	/**************************************创建或者修改文档结束**************************************************************/


	/**************************************基于query dsl配置文件脚本创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentNewWithCluster(String datasourceName,String indexName,   String addTemplate, Object bean) throws ElasticSearchException;

	/**
	 *
	 * @param indexName
	 * @param addTemplate
	 * @param bean
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentNewWithCluster(String datasourceName,String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException;
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsNewWithCluster(String datasourceName,String indexName,   String addTemplate, List<?> beans) throws ElasticSearchException;

	/**
	 * For Elasticsearch 7 and 7+
	 *
	 * @param indexName
	 * @param addTemplate
	 * @param beans
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String addDateDocumentsNewWithCluster(String datasourceName,String indexName,   String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException;

	/**************************************基于query dsl配置文件脚本创建或者修改文档结束**************************************************************/
	/**
	 *
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param id
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteDocumentNewWithCluster(String datasourceName,String indexName,  String id) throws ElasticSearchException;

	/**
	 * For Elasticsearch 7 and 7+
	 *
	 * @param indexName
	 * @param id
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public abstract String deleteDocumentNewWithCluster(String datasourceName,String indexName,   String id, String refreshOption) throws ElasticSearchException;




	/**
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param index _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param type
	 * @param ids
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocumentsWithCluster(String datasourceName,String index, Class<T> type, Object... ids)  throws ElasticSearchException;
	/**
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param index _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param ids
	 * @return
	 * @throws ElasticSearchException
	 */
	public String mgetDocumentsNewWithCluster(String datasourceName,String index,  Object... ids)  throws ElasticSearchException;



	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,  Object id, Object params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param id
	 * @param params
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,  Object id, Map params, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param id
	 * @param params
	 * @param refreshOption
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,   Object id, Map params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;

	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param id
	 * @param params
	 * @param refreshOption
	 * @param detect_noop default null
	 * @param doc_as_upsert default null
	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,   Object id, Object params, String refreshOption, Boolean detect_noop, Boolean doc_as_upsert) throws ElasticSearchException;


	/**
	 * 根据路径更新文档
	 * For Elasticsearch 7 and 7+
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param index test/_doc/1
	 *             test/_doc/1/_update
	 * @param params
	 * @param updateOptions 指定更新的相关参数

	 *    refresh=wait_for
	 *    refresh=false
	 *    refresh=true
	 *    refresh
	 *    Empty string or true
	Refresh the relevant primary and replica shardsWithCluster(String datasourceName,not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	falseWithCluster(String datasourceName,the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentWithCluster(String datasourceName,String index,  Object params, ClientOptions updateOptions) throws ElasticSearchException;

	public boolean isVersionUpper7WithCluster(String datasourceName);

	/**
	 * see https://www.elastic.co/guide/en/elasticsearch/reference/current/point-in-time-api.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html#search-after
	 * @param index
	 * @param keepaliveTime
	 * @return
	 */
	public PitId requestPitIdWithCluster(String datasourceName,String index, String keepaliveTime);
	/**
	 * see https://www.elastic.co/guide/en/elasticsearch/reference/current/point-in-time-api.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/paginate-search-results.html#search-after
	 * @param pitId
	 * @return
	 */
	public String deletePitIdWithCluster(String datasourceName,String pitId);
}
