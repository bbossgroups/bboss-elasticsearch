package org.frameworkset.elasticsearch.client;

import org.apache.http.client.ResponseHandler;
import org.frameworkset.elasticsearch.ElasticSearchException;
import org.frameworkset.elasticsearch.IndexNameBuilder;
import org.frameworkset.elasticsearch.entity.*;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponse;
import org.frameworkset.elasticsearch.entity.sql.SQLRestResponseHandler;
import org.frameworkset.elasticsearch.entity.sql.SQLResult;
import org.frameworkset.elasticsearch.entity.suggest.CompleteRestResponse;
import org.frameworkset.elasticsearch.entity.suggest.PhraseRestResponse;
import org.frameworkset.elasticsearch.entity.suggest.TermRestResponse;
import org.frameworkset.elasticsearch.handler.ESAggBucketHandle;
import org.frameworkset.elasticsearch.handler.ESMapResponseHandler;
import org.frameworkset.elasticsearch.handler.ElasticSearchResponseHandler;
import org.frameworkset.elasticsearch.scroll.ScrollHandler;
import org.frameworkset.elasticsearch.serial.ESTypeReferences;
import org.frameworkset.elasticsearch.template.*;
import org.frameworkset.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.frameworkset.elasticsearch.ElasticSearchHelper.configHolder;

/**
 * 通过配置文件加载dsl模板组件
 * https://esdoc.bbossgroups.com/#/development
 */
public abstract class AbstractConfigRestClientUtil extends RestClientUtil {
	private static Logger logger = LoggerFactory.getLogger(AbstractConfigRestClientUtil.class);
	//	public String to_char(String date,String format)
//    {
//    	 SimpleDateFormat f = new SimpleDateFormat(format);
//    	 return f.format(SimpleStringUtil.stringToDate(date,format));
//
//    }
//
//    public String to_char(String date)
//    {
//    	return to_char(date,this.FORMART_ALL);
//
//    }
	protected static String java_date_format = "yyyy-MM-dd HH:mm:ss";
	protected String configFile;
	protected ConfigDSLUtil configDSLUtil = null;



//	public final Map<String,ESUtil> esutils = new HashMap<String,ESUtil>();
	/**
	 * xml 管理dsl
	 * @param client
	 * @param indexNameBuilder
	 * @param configFile
	 */
	public AbstractConfigRestClientUtil(ElasticSearchClient client, IndexNameBuilder indexNameBuilder, String configFile) {
		super(client, indexNameBuilder);
		this.configFile = configFile;
		this.configDSLUtil = configHolder.getConfigDSLUtil( client.getElasticSearch().getDslMappingDir(),configFile);
	}

	public TemplateContainer getTemplatecontext() {
		if(configDSLUtil != null) {
			return configDSLUtil.getTemplatecontext();
		}
		return null;
	}
	protected String evalTemplate(String templateName, Object params){
		return ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params);
	}

	/**
	 * 自定义dsl管理机制：可以基于redis、db、配置管理中心管理dsl，只需要实现BaseTemplateContainerImpl抽象类中的抽象方法：
	 *
	 * @param templateContainer
	 * @param client
	 * @param indexNameBuilder
	 */
	public AbstractConfigRestClientUtil(BaseTemplateContainerImpl templateContainer, ElasticSearchClient client, IndexNameBuilder indexNameBuilder) {
		super(client, indexNameBuilder);
		templateContainer.setConfigHolder(configHolder);
		this.configDSLUtil = configHolder.getConfigDSLUtil(templateContainer);
	}







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
	public String reindexByDsl(String actionUrl,String dslName,Object params){
		return this.client.executeHttp(actionUrl,ESTemplateHelper.evalTemplate(configDSLUtil,dslName, params),ClientUtil.HTTP_POST);
	}

	public String evalConfigDsl(String dslName,Object params){
		return ESTemplateHelper.evalTemplate(configDSLUtil,dslName, params);
	}
	/**
	 *
	 * Reindex does not attempt to set up the destination index.
	 * It does not copy the settings of the source index. You should set up the destination index prior to running a _reindex action, including setting up mappings, shard counts, replicas, etc.
	 * more detail see https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-reindex.html
	 *
	 * @param actionUrl reindex请求相对url，例如：
	 *                  _reindex
	 *                  _reindex?slices=5&refresh
	 * @param dslName xml配置文件中dsl对应的name
	 * @return
	 */
	public String reindexByDsl(String actionUrl,String dslName){
		return this.client.executeHttp(actionUrl,ESTemplateHelper.evalTemplate(configDSLUtil,dslName, (Map)null),ClientUtil.HTTP_POST);
	}

	public Object execute(String options) throws ElasticSearchException {
		return client.execute(this.bulkBuilder.toString(),  options);
	}

	 

	





	/**
	 * 批量创建索引
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	@Override
	public String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException {
		return addDocuments(  indexName,   indexType,  addTemplate,   beans,  null);

	}
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
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		 return ExecuteRequestUtil._addDocuments(client, configDSLUtil,indexName, indexType,addTemplate, beans, refreshOption);
	}



	/**
	 * 批量更新索引，对于按时间分区存储的索引，需要应用程序自行处理带日期时间的索引名称
	 * @param indexName
	 * @param indexType
	 * @param updateTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans) throws ElasticSearchException{
		return updateDocuments( indexName,  indexType, updateTemplate, beans,(String)null);
	}
	public String updateDocuments(String indexName, String indexType,String updateTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		return ExecuteRequestUtil._updateDocuments( client, configDSLUtil,indexName,  indexType, updateTemplate, beans, refreshOption);
	}



	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans) throws ElasticSearchException
	{
		return addDocuments(  this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate, beans);
	}
	
	public String addDateDocuments(String indexName, String indexType,String addTemplate, List<?> beans,String refreshOption) throws ElasticSearchException{
		return addDocuments(  this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate, beans,refreshOption);
	}

	/**
	 * 创建索引文档
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException{
		return addDocument(  indexName,   indexType,  addTemplate,   bean,null);
	}

	/**
	 * 创建或者更新索引文档,适应于部分更新
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @param refreshOption
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String addDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException{
		return _addDocument( client, indexName,  indexType, addTemplate,  bean, refreshOption);
	}
	protected String _addDocument(ElasticSearchRestClient client,String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException{
		StringBuilder builder = new StringBuilder();
		ClassUtil.ClassInfo classInfo = ClassUtil.getClassInfo(bean.getClass());
		Object id = BuildTool.getId(bean,classInfo);
		Object routing = BuildTool.getRouting(bean,classInfo);

		builder.append(indexName).append("/").append(indexType);
		if(id != null){
			builder.append("/").append(id);
		}
		Object parentId = BuildTool.getParentId(bean,classInfo);
		if(refreshOption != null ){
			builder.append("?").append(refreshOption);
			if(parentId != null){
				builder.append("&parent=").append(parentId);
			}
			if(routing != null){
				builder.append("&routing=").append(routing);
			}
		}
		else if(parentId != null){
			builder.append("?parent=").append(parentId);
			if(routing != null){
				builder.append("&routing=").append(routing);
			}
		}
		else if(routing != null){
			builder.append("?routing=").append(routing);
		}
		String path = builder.toString();
		builder.setLength(0);
		path = client.executeHttp(path,ESTemplateHelper.evalDocumentTemplate(configDSLUtil,builder,indexType,indexName,addTemplate,bean,"create"),ClientUtil.HTTP_POST);
		builder = null;
		return path;
	}
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param indexType
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocument(String indexName, String indexType,String addTemplate, Object bean) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate,   bean);
	}

	public String addDateDocument(String indexName, String indexType,String addTemplate, Object bean,String refreshOption) throws ElasticSearchException{
		return addDocument(this.indexNameBuilder.getIndexName(indexName),   indexType,  addTemplate,   bean,refreshOption);
	}


	@Override
	public String executeRequest(String path, String templateName) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Map) null));
	}

	@Override
	public String executeRequest(String path, String templateName, Map params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public String executeRequest(String path, String templateName, Object params) throws ElasticSearchException {
		// TODO Auto-generated method stub
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	public <T> T executeRequest(String path, String templateName, Map params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequest(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   responseHandler);
//		return this.client.executeRequest(path, evalTemplate(templateName, params), responseHandler);
	}


	public <T> T executeRequest(String path, String templateName, Object params, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeRequest(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), responseHandler);
//		return this.client.executeRequest(path, evalTemplate(templateName, params), responseHandler);
	}



	/**
	 *
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html
	 * @param path twitter/_delete_by_query?routing=1
	 *             twitter/_doc/_delete_by_query?conflicts=proceed
	 *             twitter/_delete_by_query
	 *             twitter/_delete_by_query?scroll_size=5000
	 *
	 * @param templateName
	 * @return
	 * @throws ElasticSearchException
	 */
	public String deleteByQuery(String path,String templateName) throws ElasticSearchException{
		return this.client.executeHttp(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null),ClientUtil.HTTP_POST);
	}
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
	public String deleteByQuery(String path,String templateName,Map params) throws ElasticSearchException{
		return this.client.executeHttp(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),ClientUtil.HTTP_POST);
	}
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
	public String deleteByQuery(String path,String templateName,Object params) throws ElasticSearchException{
		return this.client.executeHttp(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),ClientUtil.HTTP_POST);
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
	public String executeHttp(String path, String templateName, String action) throws ElasticSearchException {
		// TODO Auto-generated method stub
//		return this.client.executeHttp(path, evalTemplate(templateName, (Object) null),action);
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null),   action);
	}


	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param templateName
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  executeHttp(String path, String templateName,String action,Map params,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   action,responseHandler);
	}

	/**
	 * 发送es restful请求，获取String类型json报文
	 * @param path
	 * @param templateName 请求报文
	 * @param action get,post,put,delete
	 * @return
	 * @throws ElasticSearchException
	 */
	public String executeHttp(String path, String templateName,Map params, String action) throws ElasticSearchException{
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   action);
	}
	/**
	 * 发送es restful请求，获取返回值，返回值类型由ResponseHandler决定
	 * @param path
	 * @param templateName
	 * @param action
	 * @param responseHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  executeHttp(String path, String templateName,String action,Object bean,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, bean),   action,responseHandler);
	}
	@Override
	public <T> T  executeHttp(String path, String templateName,String action,ResponseHandler<T> responseHandler) throws ElasticSearchException {
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null),   action,responseHandler);
	}

		/**
		 * 发送es restful请求，返回String类型json报文
		 * @param path
		 * @param templateName 请求报文dsl名称，在配置文件中指定
		 * @param action get,post,put,delete
		 * @return
		 * @throws ElasticSearchException
		 */
	public String executeHttp(String path, String templateName,Object bean, String action) throws ElasticSearchException{
		return super.executeHttp(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, bean),   action);
	}


	@Override
	public <T> T executeRequest(String path, String templateName, ResponseHandler<T> responseHandler) throws ElasticSearchException {
		// TODO Auto-generated method stub
//		return this.client.executeRequest(path, evalTemplate(templateName, (Object) null), responseHandler);
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null), responseHandler);
	}

	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T>  sql(Class<T> beanType , String templateName,Map params) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   new SQLRestResponseHandler());
		return ResultUtil.buildSQLResult(result,beanType);
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> List<T>  sql(Class<T> beanType , String templateName,Object bean) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, bean),   new SQLRestResponseHandler());
		return ResultUtil.buildSQLResult(result,beanType);
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T>  sql(Class<T> beanType,  String templateName) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Map)null),   new SQLRestResponseHandler());
		return ResultUtil.buildSQLResult(result,beanType);
	}

	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T> fetchQuery(Class<T> beanType , String templateName , Map params) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   new SQLRestResponseHandler());
		SQLResult<T> datas = ResultUtil.buildFetchSQLResult(  result,  beanType,  (SQLResult<T> )null);
		datas.setClientInterface(this);
		return datas;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> SQLResult<T> fetchQuery(Class<T> beanType , String templateName , Object bean) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, bean),   new SQLRestResponseHandler());
		SQLResult<T> datas = ResultUtil.buildFetchSQLResult(  result,  beanType,  (SQLResult<T> )null);
		datas.setClientInterface(this);
		return datas;
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> SQLResult<T>  fetchQuery(Class<T> beanType,  String templateName) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Map)null),   new SQLRestResponseHandler());
		SQLResult<T> datas = ResultUtil.buildFetchSQLResult(  result,  beanType,  (SQLResult<T> )null);
		datas.setClientInterface(this);
		return datas;
	}

	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> T  sqlObject(Class<T> beanType , String templateName,Map params) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   new SQLRestResponseHandler());
		return ResultUtil.buildSQLObject(result,beanType);
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> T  sqlObject(Class<T> beanType , String templateName,Object bean) throws ElasticSearchException {
        if(bean instanceof Map){
            return sqlObject( beanType ,  templateName,(Map) bean);
        }
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, bean),   new SQLRestResponseHandler());
		return ResultUtil.buildSQLObject(result,beanType);
	}
	/**
	 * 发送es restful sql请求/_xpack/sql，获取返回值，返回值类型由beanType决定
	 * @param beanType
	 * @param templateName

	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> T  sqlObject(Class<T> beanType,  String templateName) throws ElasticSearchException {
		SQLRestResponse result = this.client.executeRequest(client.getSqlRestapi(),ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Map)null),   new SQLRestResponseHandler());
		return ResultUtil.buildSQLObject(result,beanType);
	}

	@Override
	public MapRestResponse search(String path, String templateName, Map params) throws ElasticSearchException {
//		return super.executeRequest(path, evalTemplate(templateName, params), new ElasticSearchResponseHandler());
		return super.search(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public MapRestResponse search(String path, String templateName, Object params) throws ElasticSearchException {
//		return super.executeRequest(path, evalTemplate(templateName, params), new ElasticSearchResponseHandler());
		return super.search(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public MapRestResponse search(String path, String templateName) throws ElasticSearchException {
		return super.search(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null));
//		return super.executeRequest(path, evalTemplate(templateName, (Object) null), new ElasticSearchResponseHandler());
	}

	public long count(String index,String templateName)  throws ElasticSearchException{
		return  super.count(index, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null));
	}
	public long count(String index,String templateName,Map params)  throws ElasticSearchException{
		return super.count(index, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}
	public long count(String index,String templateName,Object params)  throws ElasticSearchException{
		return super.count(index, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public RestResponse search(String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
//		return super.executeRequest(path, evalTemplate(templateName, params), new ElasticSearchResponseHandler(type));
		return super.search(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), type);
	}


	@Override
	public RestResponse search(String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
//		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
		return super.search(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), type);
	}

	@Override
	public RestResponse search(String path, String templateName, Class<?> type) throws ElasticSearchException {
//		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
		return super.search(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null), type);
	}


	public <T> ESDatas<T> searchList(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, this.evalTemplate(templateName, params), new ElasticSearchResponseHandler(type));
//		return buildESDatas(result, type);
		return super.searchList(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html#msearch-cancellation
     * @param path
     * @param dslName
     * @param type
     * @return
     * @param <T>
     * @throws ElasticSearchException
     */
    public <T> List<ESDatas<T>> msearchList(String path, String dslName,Class<T> type) throws ElasticSearchException{
        return super.msearchList(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,dslName, null),type);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html#msearch-cancellation
     * @param path
     * @param dslName
     * @param params
     * @param type
     * @return
     * @param <T>
     * @throws ElasticSearchException
     */
    public <T> List<ESDatas<T>> msearchList(String path, String dslName, Map params,Class<T> type) throws ElasticSearchException{
        return super.msearchList(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,dslName, params),type);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html#msearch-cancellation
     * @param path
     * @param entity
     * @param type
     * @return
     * @throws ElasticSearchException
     */
    public  List<RestResponse> msearch(String path, String entity,Class<?> type) throws ElasticSearchException{
        return super.msearch(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,entity, null),type);
    }

    /**
     * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-multi-search.html#msearch-cancellation
     * @param path
     * @param dslName
     * @param params
     * @param type
     * @return
     * @throws ElasticSearchException
     */
    public  List<RestResponse> msearch(String path, String dslName, Map params,Class<?> type) throws ElasticSearchException{
        return super.msearch(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,dslName, params),type);
    }



    public <T> ESDatas<T> searchList(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
//		return buildESDatas(result, type);
		return super.searchList(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}

	public <T> ESDatas<T> searchList(String path, String templateName, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
//		return buildESDatas(result, type);
		return super.searchList(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Map)null),type);
	}


	public <T> T searchObject(String path, String templateName, Map params, Class<T> type) throws ElasticSearchException {
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
//		return buildObject(result, type);
		return super.searchObject(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}

	public <T> T searchObject(String path, String templateName, Object params, Class<T> type) throws ElasticSearchException {
		return super.searchObject(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
//		return buildObject(result, type);
	}

	public <T> T searchObject(String path, String templateName, Class<T> type) throws ElasticSearchException {
		return super.searchObject(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null),type);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
//		return buildObject(result, type);

	}


	@Override
	public RestResponse search(String path, String templateName, Map params, ESTypeReferences type) throws ElasticSearchException {
		return super.search(  path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   type);
	}

	@Override
	public RestResponse search(String path, String templateName, Object params, ESTypeReferences type) throws ElasticSearchException {
		return super.search(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   type);
//		return super.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler(type));
	}

	@Override
	public RestResponse search(String path, String templateName, ESTypeReferences type) throws ElasticSearchException {
		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null), new ElasticSearchResponseHandler(type));
	}


	/**
	 * 更新索引定义：my_index/_mapping
	 * 	  https://www.elastic.co/guide/en/elasticsearch/reference/7.0/indices-put-mapping.html
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName) throws ElasticSearchException {
		return super.updateIndiceMapping(action, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null));
	}

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
	public String createIndiceMapping(String indexName, String templateName) throws ElasticSearchException {
		return super.createIndiceMapping(indexName, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null));
	}


	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName, Object parameter) throws ElasticSearchException {
		return super.updateIndiceMapping(action, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, parameter));
	}

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
	public String createIndiceMapping(String indexName, String templateName, Object parameter) throws ElasticSearchException {
		return super.createIndiceMapping(indexName, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, parameter));
	}

	/**
	 * 更新索引定义
	 *
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateIndiceMapping(String action, String templateName, Map parameter) throws ElasticSearchException {
		return super.updateIndiceMapping(action, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, parameter));
	}

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
	public String createIndiceMapping(String indexName, String templateName, Map parameter) throws ElasticSearchException {
		return super.createIndiceMapping(indexName, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, parameter));
	}

	public Map<String, Object> searchMap(String path, String templateName, Map params) throws ElasticSearchException {
		return super.searchMap(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
//		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ESMapResponseHandler());
	}


	@SuppressWarnings("unchecked")
	public Map<String, Object> searchMap(String path, String templateName, Object params) throws ElasticSearchException {
		return super.searchMap(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
//		return this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ESMapResponseHandler());
	}

	/**
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> searchMap(String path, String templateName) throws ElasticSearchException {
		return super.executeRequest(path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null), new ESMapResponseHandler());
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Map params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   type,   aggs,   stats);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Object params, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), type, aggs, stats);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Class<T> type, String aggs, String stats) throws ElasticSearchException {
		return super.searchAgg(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null),   type,   aggs,   stats);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Map params, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   type,   aggs,   stats, aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Object params, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), type, aggs, stats,  aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Class<T> type, String aggs, String stats,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null),   type,   aggs,   stats, aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}


	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Map params, Class<T> type, String aggs) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   type,   aggs);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Object params, Class<T> type, String aggs) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), type, aggs);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Class<T> type, String aggs) throws ElasticSearchException {
		return super.searchAgg(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null),   type,   aggs);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Map params, Class<T> type, String aggs,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),   type,   aggs,   aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Object params, Class<T> type, String aggs, ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path,   ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params), type, aggs,  aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, params), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}

	public <T extends AggHit> ESAggDatas<T> searchAgg(String path, String templateName, Class<T> type, String aggs,ESAggBucketHandle<T> aggBucketHandle) throws ElasticSearchException {
		return super.searchAgg(  path, ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object) null),   type,   aggs,   aggBucketHandle);
//		SearchResult result = this.client.executeRequest(path, ESTemplateHelper.evalTemplate(esUtil,templateName, (Object) null), new ElasticSearchResponseHandler());
//		return buildESAggDatas(result, type, aggs, stats);
	}



	@Override
	public String createTempate(String template, String templateName) throws ElasticSearchException {
		return super.createTempate(template,ESTemplateHelper.evalTemplate(configDSLUtil,templateName,(Object)null));
	}

	@Override
	public String createTempate(String template, String templateName,Object params) throws ElasticSearchException {
		return super.createTempate(template,ESTemplateHelper.evalTemplate(configDSLUtil,templateName,(Object)params));
	}

	@Override
	public String createTempate(String template, String templateName,Map params) throws ElasticSearchException {
		return super.createTempate(template,ESTemplateHelper.evalTemplate(configDSLUtil,templateName,(Object)params));
	}

	@Override
	public TermRestResponse termSuggest(String path, String templateName, Object params) throws ElasticSearchException {
		return super.termSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public PhraseRestResponse phraseSuggest(String path, String templateName, Object params) throws ElasticSearchException {
		return super.phraseSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}



	@Override
	public TermRestResponse termSuggest(String path, String templateName, Map params) throws ElasticSearchException {
		return super.termSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public TermRestResponse termSuggest(String path, String templateName) throws ElasticSearchException {
		return super.termSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null));
	}

	@Override
	public PhraseRestResponse phraseSuggest(String path, String templateName, Map params) throws ElasticSearchException {
		return super.phraseSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	@Override
	public PhraseRestResponse phraseSuggest(String path, String templateName) throws ElasticSearchException {
		return super.phraseSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null));
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String templateName, Object params, Class<?> type) throws ElasticSearchException {
		return super.complateSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String templateName, Class<?> type) throws ElasticSearchException {
		return super.complateSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName,  (Object)null),type);
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String templateName, Map params, Class<?> type) throws ElasticSearchException {
		return super.complateSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}

	@Override
	public CompleteRestResponse complateSuggest(String path, String templateName) throws ElasticSearchException {
		return super.complateSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null));
	}

	public CompleteRestResponse complateSuggest(String path, String templateName,Map params) throws ElasticSearchException{
		return super.complateSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	public CompleteRestResponse complateSuggest(String path, String templateName,Object params) throws ElasticSearchException{
		return super.complateSuggest(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params));
	}

	/**
	 * 根据路径更新文档
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html
	 * @param path test/_doc/1
	 *             test/_doc/1/_update
	 *
	 *
	 * @param templateName
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByPath(String path,String templateName) throws ElasticSearchException{
		try {
			return this.client.executeHttp(path, ESTemplateHelper.evalTemplate(configDSLUtil, templateName, (Object) null), ClientUtil.HTTP_POST);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateDocument);
		}
	}

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
	public String updateByPath(String path,String templateName,Map params) throws ElasticSearchException{
		try {
			return this.client.executeHttp(path, ESTemplateHelper.evalTemplate(configDSLUtil, templateName, params), ClientUtil.HTTP_POST);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateDocument);
		}
	}

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
	public String updateByPath(String path,String templateName,Object params) throws ElasticSearchException{
		try {
			return this.client.executeHttp(path, ESTemplateHelper.evalTemplate(configDSLUtil, templateName, params), ClientUtil.HTTP_POST);
		}
		catch(ElasticSearchException e){
			return ResultUtil.hand404HttpRuntimeException(e,String.class,ResultUtil.OPERTYPE_updateDocument);
		}
	}

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
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateByQuery(String path,String templateName) throws ElasticSearchException{
		return this.client.executeHttp(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null),ClientUtil.HTTP_POST);
	}

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
	 */
	public String updateByQuery(String path,String templateName,Map params) throws ElasticSearchException{
		return this.client.executeHttp(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),ClientUtil.HTTP_POST);
	}

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
	 */
	public String updateByQuery(String path,String templateName,Object params) throws ElasticSearchException{
		return this.client.executeHttp(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),ClientUtil.HTTP_POST);
	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-multi-get.html
	 * @param path _mget
	 *             test/_mget
	 *             test/type/_mget
	 *             test/type/_mget?stored_fields=field1,field2
	 *             _mget?routing=key1
	 * @param templateName
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> List<T> mgetDocuments(String path,String templateName,Class<T> type)  throws ElasticSearchException{
		return super.mgetDocuments(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, (Object)null),type);
	}

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
	public <T> List<T> mgetDocuments(String path,String templateName,Object params,Class<T> type)  throws ElasticSearchException{
		return super.mgetDocuments(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}

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
	public <T> List<T> mgetDocuments(String path,String templateName,Map params,Class<T> type)  throws ElasticSearchException{
		return super.mgetDocuments(path,ESTemplateHelper.evalTemplate(configDSLUtil,templateName, params),type);
	}


	public ESInfo getESInfo(String templateName){
		return  configDSLUtil.getESInfo(templateName);
	}

	public <T> ESDatas<T> scrollSliceParallel(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type,
									  ScrollHandler<T> scrollHandler) throws ElasticSearchException{

		return ExecuteRequestUtil.scrollSliceParallel( this, configDSLUtil,path, dslTemplate,params ,		 scroll  , type,		scrollHandler);

	}


	/**
	 * 一次性返回scroll检索结果
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public  <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Map params,Class<T> type) throws ElasticSearchException{
		return super.scroll(path,ESTemplateHelper.evalTemplate(configDSLUtil,dslTemplate, params),scroll,type);
	}


	/**
	 * scroll检索,每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Map params,Class<T> type,ScrollHandler<T> scrollHandler)
			throws ElasticSearchException{
		return super.scroll(path,ESTemplateHelper.evalTemplate(configDSLUtil,dslTemplate, params),scroll,type,scrollHandler);
	}

	public <T> ESDatas<T> scrollParallel(String path,String dslTemplate,String scroll,Map params,Class<T> type,ScrollHandler<T> scrollHandler)
			throws ElasticSearchException{
		return super.scrollParallel(path,ESTemplateHelper.evalTemplate(configDSLUtil,dslTemplate, params),scroll,type,scrollHandler);
	}
	/**
	 * scroll检索,每次检索结果交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param scrollHandler
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Object params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		return super.scroll(path,ESTemplateHelper.evalTemplate(configDSLUtil,dslTemplate, params),scroll,type,scrollHandler);
	}

	public   <T> ESDatas<T> scrollParallel(String path,String dslTemplate,String scroll,Object params,Class<T> type,ScrollHandler<T> scrollHandler) throws ElasticSearchException{
		return super.scrollParallel(path,ESTemplateHelper.evalTemplate(configDSLUtil,dslTemplate, params),scroll,type,scrollHandler);
	}
	/**
	 * 一次性返回scroll检索结果
	 * @param path
	 * @param dslTemplate
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public   <T> ESDatas<T> scroll(String path,String dslTemplate,String scroll,Object params,Class<T> type) throws ElasticSearchException{
		return super.scroll(path,ESTemplateHelper.evalTemplate(configDSLUtil,dslTemplate, params),scroll,type);
	}


	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain sliceId,sliceMax varialbe
	 * 	 * 	 *    <property name="scrollSliceQuery">
	 * 	 * 	 *         <![CDATA[
	 * 	 * 	 *          {
	 * 	 * 	 *            "slice": {
	 * 	 * 	 *                 "id": #[sliceId],
	 * 	 * 	 *                 "max": #[sliceMax]
	 * 	 * 	 *             },
	 * 	 * 	 *             "size":#[size],
	 * 	 * 	 *             "query": {
	 * 	 * 	 *                 "term" : {
	 * 	 * 	 *                     "gc.jvmGcOldCount" : 3
	 * 	 * 	 *                 }
	 * 	 * 	 *             }
	 * 	 * 	 *         }
	 * 	 * 	 *         ]]>
	 * 	 * 	 *     </property>
	 * @param scroll
	 * @param type
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSlice(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type) throws ElasticSearchException{
		return scrollSlice(  path,  dslTemplate, params, scroll ,    type,(ScrollHandler<T>)null);
	}

	public <T> ESDatas<T> scrollSliceParallel(String path,final String dslTemplate,final Map params ,
									  final String scroll  ,final Class<T> type) throws ElasticSearchException{
		return scrollSliceParallel(  path,  dslTemplate, params, scroll ,    type,(ScrollHandler<T>)null);
	}


	/**
	 * slice scroll并行检索，每次检索结果列表交给scrollHandler回调函数处理
	 * @param path
	 * @param dslTemplate here is a example dsltemplate: must contain sliceId,sliceMax varialbe
     *                       
	 * 	 * 	 *    <property name="scrollSliceQuery">
	 * 	 * 	 *         <![CDATA[
	 * 	 * 	 *          {
	 * 	 * 	 *            "slice": {
	 * 	 * 	 *                 "id": #[sliceId],
	 * 	 * 	 *                 "max": #[sliceMax]
	 * 	 * 	 *             },
	 * 	 * 	 *             "size":#[size],
	 * 	 * 	 *             "query": {
	 * 	 * 	 *                 "term" : {
	 * 	 * 	 *                     "gc.jvmGcOldCount" : 3
	 * 	 * 	 *                 }
	 * 	 * 	 *             }
	 * 	 * 	 *         }
	 * 	 * 	 *         ]]>
	 * 	 * 	 *     </property>
	 * @param params 查询参数
	 * @param scroll
	 * @param type
	 * @param scrollHandler 每次检索结果会被异步交给handle来处理
	 * @param <T>
	 * @return
	 * @throws ElasticSearchException
	 */
	public <T> ESDatas<T> scrollSlice(String path,final String dslTemplate,Map params ,
									  final String scroll  ,final Class<T> type,
									  ScrollHandler<T> scrollHandler) throws ElasticSearchException{
			return  ExecuteRequestUtil._scrollSlice( client, configDSLUtil,path, dslTemplate,  params ,
					scroll  ,  type,
					scrollHandler);

	}

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
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String addDocumentsNew(String indexName,   String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException{
		return addDocuments( indexName,   _doc,addTemplate,  beans,refreshOption);
	}
	public   String addDocumentsNew(String indexName,  String addTemplate, List<?> beans) throws ElasticSearchException{
		return addDocuments( indexName,   _doc,addTemplate,  beans);
	}

	/**
	 * 创建或者更新索引文档
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDocumentNew(String indexName,  String addTemplate, Object bean) throws ElasticSearchException{
		return addDocument( indexName,    _doc,addTemplate,  bean);
	}

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
	public String addDocumentNew(String indexName,   String addTemplate, Object bean, String refreshOption) throws ElasticSearchException{
		return addDocument( indexName,    _doc,addTemplate,  bean,  refreshOption);

	}

	public  String updateDocumentsNew(String indexName,   String updateTemplate, List<?> beans) throws ElasticSearchException{
		return updateDocuments(  indexName,    _doc,   updateTemplate,   beans);
	}

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
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public String updateDocumentsNew(String indexName , String updateTemplate, List<?> beans, String refreshOption) throws ElasticSearchException{
		return updateDocuments(  indexName ,  _doc, updateTemplate,   beans,   refreshOption);
	}

	/***************************读取模板文件添加或者修改文档结束************************************/

	/**************************************基于query dsl配置文件脚本创建或者修改文档开始**************************************************************/
	/**
	 * 创建索引文档，根据elasticsearch.xml中指定的日期时间格式，生成对应时间段的索引表名称
	 * @param indexName
	 * @param addTemplate
	 * @param bean
	 * @return
	 * @throws ElasticSearchException
	 */
	public   String addDateDocumentNew(String indexName,   String addTemplate, Object bean) throws ElasticSearchException{
		return addDateDocument(  indexName,  _doc,   addTemplate,   bean);
	}

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
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentNew(String indexName, String addTemplate, Object bean, String refreshOption) throws ElasticSearchException{
		return addDateDocument(  indexName, _doc,    addTemplate,   bean,   refreshOption);
	}
	/**
	 * 批量创建索引,根据时间格式建立新的索引表
	 * For Elasticsearch 7 and 7+
	 * @param indexName
	 * @param addTemplate
	 * @param beans
	 * @return
	 * @throws ElasticSearchException
	 */
	public String addDateDocumentsNew(String indexName,   String addTemplate, List<?> beans) throws ElasticSearchException{
		return addDateDocuments( indexName, _doc,   addTemplate, beans);
	}


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
	Refresh the relevant primary and replica shards (not the whole index) immediately after the operation occurs, so that the updated document appears in search results immediately. This should ONLY be done after careful thought and verification that it does not lead to poor performance, both from an indexing and a search standpoint.
	wait_for
	Wait for the changes made by the request to be made visible by a refresh before replying. This doesn’t force an immediate refresh, rather, it waits for a refresh to happen. Elasticsearch automatically refreshes shards that have changed every index.refresh_interval which defaults to one second. That setting is dynamic. Calling the Refresh API or setting refresh to true on any of the APIs that support it will also cause a refresh, in turn causing already running requests with refresh=wait_for to return.
	false (the default)
	Take no refresh related actions. The changes made by this request will be made visible at some point after the request returns.
	 * @return
	 * @throws ElasticSearchException
	 */
	public  String addDateDocumentsNew(String indexName,   String addTemplate, List<?> beans, String refreshOption) throws ElasticSearchException{
		return addDateDocuments( indexName,    _doc, addTemplate,  beans,   refreshOption);
	}

	/**************************************基于query dsl配置文件脚本创建或者修改文档结束**************************************************************/
	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 *
	 * @param scriptName
	 * @param scriptDslTemplate
	 * @return
	 */
	public String createScript(String scriptName,String scriptDslTemplate){
		return this.client.executeHttp(new StringBuilder().append("_scripts/").append(scriptName).toString(),
				ESTemplateHelper.evalTemplate(configDSLUtil,scriptDslTemplate, (Object)null),ClientUtil.HTTP_POST);
	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 *
	 * @param scriptName
	 * @param scriptDslTemplate
	 * @return
	 */
	public String createScript(String scriptName,String scriptDslTemplate,Map params){
		return this.client.executeHttp(new StringBuilder().append("_scripts/").append(scriptName).toString(),
				ESTemplateHelper.evalTemplate(configDSLUtil,scriptDslTemplate, params),ClientUtil.HTTP_POST);
	}

	/**
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-scripting-using.html
	 * https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-function-score-query.html
	 *
	 * @param scriptName
	 * @param scriptDslTemplate
	 * @return
	 */
	public String createScript(String scriptName,String scriptDslTemplate,Object params){
		return this.client.executeHttp(new StringBuilder().append("_scripts/").append(scriptName).toString(),
				ESTemplateHelper.evalTemplate(configDSLUtil,scriptDslTemplate, params),ClientUtil.HTTP_POST);
	}



}
