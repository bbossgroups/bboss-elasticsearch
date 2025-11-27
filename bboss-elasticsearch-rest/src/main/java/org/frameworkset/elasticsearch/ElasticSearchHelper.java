package org.frameworkset.elasticsearch;

import com.frameworkset.util.BaseSimpleStringUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.frameworkset.elasticsearch.boot.ElasticsearchBootResult;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ConfigHolder;
import org.frameworkset.elasticsearch.template.AOPTemplateContainerImpl;
import org.frameworkset.elasticsearch.template.BaseTemplateContainerImpl;
import org.frameworkset.elasticsearch.template.ESSOAFileApplicationContext;
import org.frameworkset.spi.BaseApplicationContext;
import org.frameworkset.spi.DefaultApplicationContext;
import org.frameworkset.spi.assemble.GetProperties;
import org.frameworkset.util.shutdown.ShutdownUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ElasticSearchHelper {
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchHelper.class);
	protected static DefaultApplicationContext context = null;
	public static final String DEFAULT_SEARCH = "elasticSearch";
	protected static ElasticSearch elasticSearchSink = null;

	private static final Map<String,Object> geoipConfig = new ConcurrentHashMap<String, Object>();
	private static boolean inited;
	// # dsl配置文件热加载扫描时间间隔，毫秒为单位，默认5秒扫描一次，<= 0时关闭扫描机制
	private static long dslfileRefreshInterval = 5000;
	private static String dslfileMappingDir;
	private static Method bootMethod;
	public final static ConfigHolder configHolder = new ConfigHolder("Elasticsearch");

	static
	{
        ESVersionInfo.getESVersion();
		ShutdownUtil.addShutdownHook(new Runnable(){

			public void run() {
//				ESUtil.stopmonitor();
//				destory(esutils);
				configHolder.stopmonitor();
				configHolder.destory();

			}});
	}

	static {
		try {
			Class booterClass = Class.forName("org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot");
			bootMethod = booterClass.getMethod("boot");
		} catch (ClassNotFoundException e) {
			if(logger.isWarnEnabled()){
				logger.warn("ElasticSearch load from Boot ignore: org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot Not found!");
			}
		} catch (NoSuchMethodException e) {
			if(logger.isWarnEnabled()){
				logger.warn("ElasticSearch load from Boot ignore: boot method Not found in org.frameworkset.elasticsearch.boot.ElasticSearchConfigBoot!");
			}
		}
	}
	public static Map<String,Object> getGeoipConfig(){
		return geoipConfig;
	}
	public static long getDslfileRefreshInterval(){
		return dslfileRefreshInterval;
	}
	public static String getDslfileMappingDir(){
		return dslfileMappingDir;
	}
	public static void setDslfileRefreshInterval(long dslfileRefreshInterval){
		ElasticSearchHelper.dslfileRefreshInterval = dslfileRefreshInterval;
	}
	private static final Map<String,ElasticSearch> elasticSearchMap = new ConcurrentHashMap<String, ElasticSearch>();


	/**
	 * 引用elasticsearch数据源
	 */
	private static final Map<String,String> referElasticSearchMap = new ConcurrentHashMap<String, String>();

	/**
	 * 获取所有的es数据源配置对象
	 * @return
	 */
	public static Map<String,ElasticSearch> getAllElasticSearches(){
		LinkedHashMap temp = new LinkedHashMap<String, ElasticSearch>();
		temp.putAll(elasticSearchMap);
		return temp;
	}
	/**
	 * 获取所有的被定义为引用其他数据源的es数据源配置信息
	 * @return Map<elasticsearchDatasource,referedElasticsearchDatasource>
	 */
	public static Map<String,String> getAllReferElasticSearches(){
		LinkedHashMap temp = new LinkedHashMap<String, ElasticSearch>();
		temp.putAll(referElasticSearchMap);
		return temp;
	}
	public ElasticSearchHelper() {
		// TODO Auto-generated constructor stub
	}
	/**
	 *  <property name="elasticsearch.client" value="${elasticsearch.client:restful}">
	 *                 <description> <![CDATA[ 客户端类型:transport，restful ]]></description>
	 *             </property>
	 *
	 *             <property name="elasticUser" value="${elasticUser:}">
	 *                 <description> <![CDATA[ 认证用户 ]]></description>
	 *             </property>
	 *
	 *             <property name="elasticPassword" value="${elasticPassword:}">
	 *                 <description> <![CDATA[ 认证口令 ]]></description>
	 *             </property>
	 *             <!--<property name="elasticsearch.hostNames" value="${elasticsearch.hostNames}">
	 *                 <description> <![CDATA[ 指定序列化处理类，默认为kafka.serializer.DefaultEncoder,即byte[] ]]></description>
	 *             </property>-->
	 *
	 *             <property name="elasticsearch.rest.hostNames" value="${elasticsearch.rest.hostNames:127.0.0.1:9200}">
	 *                 <description> <![CDATA[ rest协议地址 ]]></description>
	 *             </property>
	 *
	 *
	 *             <property name="elasticsearch.dateFormat" value="${elasticsearch.dateFormat:yyyy.MM.dd}">
	 *                 <description> <![CDATA[ 索引日期格式]]></description>
	 *             </property>
	 *             <property name="elasticsearch.timeZone" value="${elasticsearch.timeZone:Asia/Shanghai}">
	 *                 <description> <![CDATA[ 时区信息]]></description>
	 *             </property>
	 *
	 *             <property name="elasticsearch.ttl" value="${elasticsearch.ttl:2d}">
	 *                 <description> <![CDATA[ ms(毫秒) s(秒) m(分钟) h(小时) d(天) w(星期)]]></description>
	 *             </property>
	 *
	 *             <property name="elasticsearch.showTemplate" value="${elasticsearch.showTemplate:false}">
	 *                 <description> <![CDATA[ query dsl脚本日志调试开关，与log info级别日志结合使用]]></description>
	 *             </property>
	 *
	 *             <property name="elasticsearch.httpPool" value="${elasticsearch.httpPool:default}">
	 *                 <description> <![CDATA[ http连接池逻辑名称，在conf/httpclient.xml中配置]]></description>
	 *             </property>
	 *             <property name="elasticsearch.discoverHost" value="${elasticsearch.discoverHost:false}">
	 *                 <description> <![CDATA[ 是否启动节点自动发现功能，默认关闭，开启后每隔10秒探测新加或者移除的es节点，实时更新本地地址清单]]></description>
	 *             </property>
	 */
	public static List<String> booter(String[] elasticsearchServerNames,GetProperties configContext,boolean forceBoot){
		return booter(  elasticsearchServerNames,  configContext,  forceBoot,false);
	}

	/**
	 *
	 * @param elasticsearchServerNames
	 * @param configContext
	 * @param forceBoot
	 * @param fromspringboot
	 * @return 返回启动的elasticsearch数据源清单列表
	 */

	public static List<String> booter(String[] elasticsearchServerNames,GetProperties configContext,boolean forceBoot,boolean fromspringboot){
		if(inited ) {
			if(!forceBoot)
				return null;
		}
		inited = true;
		ElasticSearch elasticSearchSink = null;
		ElasticSearch firstElasticSearch = null;
		initDslFileRefreshInterval( configContext);
		Map<String,ElasticSearch> elasticSearchMap = new LinkedHashMap<String, ElasticSearch>();
		List<String> initedElasticsearchs = new ArrayList<>();
		Map<String,String> referElasticSearchMap = new LinkedHashMap<String, String>();
		for(String serverName:elasticsearchServerNames){
			if(ElasticSearchHelper.elasticSearchMap.containsKey(serverName) || ElasticSearchHelper.referElasticSearchMap.containsKey(serverName))
				continue;
			else if (serverName.equals("default")){
				if(ElasticSearchHelper.elasticSearchMap.containsKey(DEFAULT_SEARCH) || ElasticSearchHelper.referElasticSearchMap.containsKey(DEFAULT_SEARCH))
					continue;
			}
//			# 直接与指标数据库保持一致，直接引用即可

			String referExternal = ElasticSearchHelper._getStringValue(serverName,"elasticsearch.referExternal",configContext,null);
			if(referExternal == null || referExternal.equals("")) {
				Properties elasticsearchPropes = new Properties();
				elasticsearchPropes.put("elasticsearch.client",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.client", configContext, "restful"));
				elasticsearchPropes.put("elasticUser",
						ElasticSearchHelper._getStringValue(serverName, "elasticUser", configContext, ""));
				elasticsearchPropes.put("elasticPassword",
						ElasticSearchHelper._getStringValue(serverName, "elasticPassword", configContext, ""));


				elasticsearchPropes.put("elasticsearch.rest.hostNames",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.rest.hostNames", configContext, "127.0.0.1:9200"));
				elasticsearchPropes.put("elasticsearch.dateFormat",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.dateFormat", configContext, "yyyy.MM.dd"));
				elasticsearchPropes.put("elasticsearch.timeZone",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.timeZone", configContext, "Asia/Shanghai"));
				elasticsearchPropes.put("elasticsearch.ttl",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.ttl", configContext, "2d"));
				elasticsearchPropes.put("elasticsearch.showTemplate",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.showTemplate", configContext, "false"));

                elasticsearchPropes.put("elasticsearch.healthPath",
                        ElasticSearchHelper._getStringValue(serverName, "elasticsearch.healthPath", configContext, "/"));
				elasticsearchPropes.put("elasticsearch.httpPool",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.httpPool", configContext, serverName));
				elasticsearchPropes.put("elasticsearch.discoverHost",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.discoverHost", configContext, "false"));
				elasticsearchPropes.put("elasticsearch.version",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.version", configContext, "7.0.0"));

				elasticsearchPropes.put("elasticsearch.sliceScrollThreadCount",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.sliceScrollThreadCount", configContext, "50"));
				elasticsearchPropes.put("elasticsearch.sliceScrollThreadQueue",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.sliceScrollThreadQueue", configContext, "100"));
				elasticsearchPropes.put("elasticsearch.sliceScrollBlockedWaitTimeout",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.sliceScrollBlockedWaitTimeout", configContext, "0"));
				elasticsearchPropes.put("elasticsearch.includeTypeName",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.includeTypeName", configContext, "false"));
				elasticsearchPropes.put("elasticsearch.scrollThreadCount",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.scrollThreadCount", configContext, "50"));
				elasticsearchPropes.put("elasticsearch.scrollThreadQueue",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.scrollThreadQueue", configContext, "200"));
				elasticsearchPropes.put("elasticsearch.scrollBlockedWaitTimeout",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.scrollBlockedWaitTimeout", configContext, "0"));
				elasticsearchPropes.put("elasticsearch.healthCheckInterval",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.healthCheckInterval", configContext, "3000"));

				elasticsearchPropes.put("elasticsearch.failAllContinue",
						ElasticSearchHelper._getStringValue(serverName, "elasticsearch.failAllContinue", configContext, "true"));
				String slowDslThreshold = ElasticSearchHelper._getStringValue(serverName, "elasticsearch.slowDslThreshold", configContext, null);
				if (slowDslThreshold != null) {
					elasticsearchPropes.put("elasticsearch.slowDslThreshold",
							slowDslThreshold);
				}
				String slowDslCallback = ElasticSearchHelper._getStringValue(serverName, "elasticsearch.slowDslCallback", configContext, null);
				if (slowDslCallback != null) {
					elasticsearchPropes.put("elasticsearch.slowDslCallback",
							slowDslCallback);
				}
				String logDslCallback = ElasticSearchHelper._getStringValue(serverName, "elasticsearch.logDslCallback", configContext, null);
				if (logDslCallback != null) {
					elasticsearchPropes.put("elasticsearch.logDslCallback",
							logDslCallback);
				}

				String useHttps = ElasticSearchHelper._getStringValue(serverName, "elasticsearch.useHttps", configContext, null);
				if (useHttps != null) {
					elasticsearchPropes.put("elasticsearch.useHttps",
							useHttps);
				}

                String sqlRestapi = ElasticSearchHelper._getStringValue(serverName, "elasticsearch.sqlRestapi", configContext, null);
                if (sqlRestapi != null) {
                    elasticsearchPropes.put("elasticsearch.sqlRestapi",
                            sqlRestapi);
                }                        
				final ElasticSearch elasticSearch = new ElasticSearch();
				if (firstElasticSearch == null)
					firstElasticSearch = elasticSearch;
				elasticSearch.setFromspringboot(fromspringboot);
				elasticSearch.setElasticSearchName(serverName);
				elasticSearch.setElasticsearchPropes(elasticsearchPropes);
				elasticSearch.configureWithConfigContext(configContext);
				if (!serverName.equals("default")) {

					elasticSearchMap.put(serverName, elasticSearch);
				} else {
					elasticSearchMap.put(DEFAULT_SEARCH, elasticSearch);
					elasticSearchSink = elasticSearch;
				}
				initedElasticsearchs.add(serverName);
			}
			else{
				if (!serverName.equals("default")) {

					referElasticSearchMap.put(serverName,referExternal);
				} else {
					referElasticSearchMap.put(DEFAULT_SEARCH,referExternal);
				}
			}


		}

		synchronized (geoipConfig) {
			if(SimpleStringUtil.isEmpty(geoipConfig.get("ip.database")) )
				geoipConfig.put("ip.database",
					ElasticSearchHelper._getStringValue("", "ip.database", configContext, ""));
			if(SimpleStringUtil.isEmpty(geoipConfig.get("ip.asnDatabase")) )
				geoipConfig.put("ip.asnDatabase",
					ElasticSearchHelper._getStringValue("", "ip.asnDatabase", configContext, ""));
			if(SimpleStringUtil.isEmpty(geoipConfig.get("ip.ispConverter")) )
				geoipConfig.put("ip.ispConverter",
					ElasticSearchHelper._getStringValue("", "ip.ispConverter", configContext, ""));
			if(SimpleStringUtil.isEmpty(geoipConfig.get("ip.ip2regionDatabase")) )
				geoipConfig.put("ip.ip2regionDatabase",
					ElasticSearchHelper._getStringValue("", "ip.ip2regionDatabase", configContext, ""));

			if(SimpleStringUtil.isEmpty(geoipConfig.get("ip.cachesize")) )
				geoipConfig.put("ip.cachesize",
					ElasticSearchHelper._getStringValue("", "ip.cachesize", configContext, "10000"));
			if(SimpleStringUtil.isEmpty(geoipConfig.get("ip.serviceUrl")) )
				geoipConfig.put("ip.serviceUrl",
					ElasticSearchHelper._getStringValue("", "ip.serviceUrl", configContext, ""));

			if (logger.isInfoEnabled()) {
				try {
					logger.info("Geo ipinfo config {},from springboot:{}", SimpleStringUtil.object2json(geoipConfig), fromspringboot);
				} catch (Exception e) {

				}
			}

		}
		if(elasticSearchSink != null){
			ElasticSearchHelper.elasticSearchSink = elasticSearchSink;
		}
		else if (ElasticSearchHelper.elasticSearchSink == null) {
			elasticSearchSink = firstElasticSearch;
			ElasticSearchHelper.elasticSearchSink = elasticSearchSink;
		}


		if(elasticSearchMap.size() > 0) {
			Iterator<Map.Entry<String, ElasticSearch>> entries = elasticSearchMap.entrySet().iterator();
			while(entries.hasNext()){
				Map.Entry<String, ElasticSearch> entry = entries.next();
				final ElasticSearch elasticSearch = entry.getValue();
				elasticSearch.start();
//				ShutdownUtil.addShutdownHook(new Runnable() {
//					@Override
//					public void run() {
//						elasticSearch.stop();
//					}
//				});
			}
			synchronized (ElasticSearchHelper.elasticSearchMap) {
				ElasticSearchHelper.elasticSearchMap.putAll(elasticSearchMap);
			}

		}
		if(referElasticSearchMap.size() > 0){
			synchronized (ElasticSearchHelper.referElasticSearchMap) {
				ElasticSearchHelper.referElasticSearchMap.putAll(referElasticSearchMap);
			}
		}
		return initedElasticsearchs;
	}

	private static long _getLongValue(String poolName,String propertyName,GetProperties context,long defaultValue) throws Exception {
		String _value = null;
		if(poolName.equals("default")){
			_value = context.getExternalProperty(propertyName);
			if(_value == null)
				_value = context.getExternalProperty(poolName+"."+propertyName);

		}
		else{
			_value = context.getExternalProperty(poolName+"."+propertyName);
		}
		if(_value == null){
			return defaultValue;
		}
		try {
			long ret = Long.parseLong(_value.trim());
			return ret;
		}
		catch (Exception e){
			throw e;
		}
	}

	private static int _getIntValue(String poolName,String propertyName,BaseApplicationContext context,int defaultValue) throws Exception {
		String _value = null;
		if(poolName.equals("default")){
			_value = context.getExternalProperty(propertyName);
			if(_value == null)
				_value = context.getExternalProperty(poolName+"."+propertyName);

		}
		else{
			_value = context.getExternalProperty(poolName+"."+propertyName);
		}
		if(_value == null){
			return defaultValue;
		}
		try {
			int ret = Integer.parseInt(_value);
			return ret;
		}
		catch (Exception e){
			throw e;
		}
	}
	public static String _getStringValue(String poolName,String propertyName,GetProperties context,String defaultValue){
		String _value = null;
		if(poolName.equals("default")){
			_value = context.getExternalProperty(propertyName);
			if(_value == null)
				_value = context.getExternalProperty(poolName+"."+propertyName);

		}
		else{
			if(!poolName.equals(""))
				_value = context.getExternalProperty(poolName+"."+propertyName);
			else{
				_value = context.getExternalProperty(propertyName);
			}
		}
		if(_value == null){
			return defaultValue;
		}
		return _value;
	}
	private static void initDslFileRefreshInterval(GetProperties context){
		try {
			long _dslfileRefreshInterval = ElasticSearchHelper._getLongValue("default","dslfile.refreshInterval",context,5000l);
			dslfileRefreshInterval = _dslfileRefreshInterval;
		} catch (Exception e) {

		}
		String _dslfileMappingDir = ElasticSearchHelper._getStringValue("default","dslfile.dslMappingDir",context,null);
		if(_dslfileMappingDir != null && !_dslfileMappingDir.trim().equals(""))
			dslfileMappingDir = _dslfileMappingDir;
		if(logger.isInfoEnabled()){
			logger.info("dslfile.refreshInterval {} ms",dslfileRefreshInterval);
			logger.info("dslfile.dslMappingDir {}",dslfileMappingDir);
		}
	}

	public static void init(){
		if(inited )
			return;
		synchronized (elasticSearchMap) {
			if(inited)
				return;
			if (elasticSearchSink == null) {
				ElasticSearch _elasticSearchSink = elasticSearchMap.get(DEFAULT_SEARCH);
				if (_elasticSearchSink == null) {
					context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
					if(!context.isEmptyContext()) {
						initDslFileRefreshInterval( context);
						_elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
						if (_elasticSearchSink != null) {
							elasticSearchMap.put(DEFAULT_SEARCH, _elasticSearchSink);
							elasticSearchSink = _elasticSearchSink;
						}
					}
				}

			}
			if(context.isEmptyContext()){
				if(bootMethod != null){
					try {
						bootMethod.invoke(null);
					} catch (IllegalAccessException e) {
						throw new ElasticsearchParseException("ElasticSearch load from Boot failed:",e);
					} catch (InvocationTargetException e) {
						throw new ElasticsearchParseException("ElasticSearch load from Boot failed:",e);
					}
				}
				else{
					if(logger.isWarnEnabled()){
						logger.warn("ElasticSearch load from Boot warn: No booter found and bootMethod is null!");
					}
				}
			}
			inited = true;

		}
	}

	public static synchronized void stopElasticsearch(String esname){
		ElasticSearch elasticSearch = getElasticSearchSinkOnly(esname,true);
		if(elasticSearch != null) {
			elasticSearch.stop();
			if(esname == null || esname.equals("") || esname.equals("default") ) {

				 elasticSearchSink = null;
				 elasticSearchMap.remove(DEFAULT_SEARCH);
				 referElasticSearchMap.remove(DEFAULT_SEARCH);
			}
			else {
				elasticSearchMap.remove(esname);
			}
		}
		referElasticSearchMap.remove(esname);
	}
	public static synchronized void stopElasticsearchs(Map<String,Object> esnames){
		if(esnames == null || esnames.size() == 0)
			return;
		Iterator<Map.Entry<String,Object>> iterator = esnames.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry<String,Object> esname = iterator.next();
			stopElasticsearch( esname.getKey());
		}

	}

    public static synchronized void stopElasticsearchs(ElasticsearchBootResult elasticsearchBootResult){
        Map<String, Object> initedElasticsearch = elasticsearchBootResult.getResourceStartResult();
        if (BaseSimpleStringUtil.isNotEmpty(initedElasticsearch)) {
            ElasticSearchHelper.stopElasticsearchs(initedElasticsearch);
        }
    }


    public static ElasticSearch getElasticSearchSinkOnly(String elasticSearch){
		return getElasticSearchSinkOnly(elasticSearch,false);
	}
	public static ElasticSearch getElasticSearchSinkOnly(String elasticSearch,boolean direct){
		if(elasticSearch == null || elasticSearch.equals("") || elasticSearch.equals("default") ) {
            return elasticSearchMap.get(DEFAULT_SEARCH);
		}
		if(!direct) {
			elasticSearch = getRealElasticserachName(elasticSearch);
			if (elasticSearch == null || elasticSearch.equals("") || elasticSearch.equals("default")) {

                return elasticSearchMap.get(DEFAULT_SEARCH);
			}
		}
		ElasticSearch elasticSearchSink = elasticSearchMap.get(elasticSearch);

		return elasticSearchSink;
	}
	public static String getRealElasticserachName(String elasticSearch){
		if(elasticSearch == null || elasticSearch.equals("") || elasticSearch.equals("default") ) {

			return DEFAULT_SEARCH;
		}
		String name = referElasticSearchMap.get(elasticSearch);
		if(name == null)
			return elasticSearch;
		do{
			String name_ = referElasticSearchMap.get(name);
			if(name_ == null) {
				break;
			}
			else{
				if(name_.equals(name)){
					throw new ElasticSearchException("Elasticsearch数据源不能直接或者间接引用自己："+name);
				}
				else{
					name = name_;
				}
			}

		}while (true);

		return name;
	}
	/**
	 * 获取elasticSearch对应的elasticSearch服务器对象
	 * @param elasticSearch
	 * @return
	 */
	public static ElasticSearch getElasticSearchSink(String elasticSearch){
		init();
		if(elasticSearch == null || elasticSearch.equals("") || elasticSearch.equals("default") ) {

			return elasticSearchSink;
		}
		elasticSearch = getRealElasticserachName( elasticSearch);
		if(elasticSearch == null || elasticSearch.equals("") || elasticSearch.equals("default") ) {

			return elasticSearchSink;
		}
		ElasticSearch elasticSearchSink = elasticSearchMap.get(elasticSearch);
		if(elasticSearchSink == null) {
			synchronized (elasticSearchMap) {
				elasticSearchSink = elasticSearchMap.get(elasticSearch);
				if(elasticSearchSink != null)
					return elasticSearchSink;
				try {
					context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
					elasticSearchSink = context.getTBeanObject(elasticSearch, ElasticSearch.class);
				}
				catch (Exception e){
					throw new ElasticSearchException("Elasticsearh Datasource[" + elasticSearch + "] is not configured. please check your configs.",e);
				}
				if (elasticSearchSink != null) {
					elasticSearchMap.put(elasticSearch, elasticSearchSink);
				}
			}
		}
		return elasticSearchSink;
	}
	
	public static ElasticSearch getElasticSearchSink(){
		init();
		return elasticSearchSink;
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件
	 * @return
	 */
	public static ClientInterface getRestClientUtil(){
		init();
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		return elasticSearchSink.getRestClientUtil();
	}

	/**
	 * 获取直接操作query dsl的rest api接口组件,所有的操作直接在elasticSearch对应的es服务器上操作
	 * @param elasticSearch
	 * @return
	 */
	public static ClientInterface getRestClientUtil(String elasticSearch){
		ElasticSearch elasticSearchSink = null;
		try {
			elasticSearchSink = getElasticSearchSink(elasticSearch);

		}
		catch (ElasticSearchException e){
			throw e;
		}
		catch (Exception e){
			throw new ElasticSearchException("Elasticsearh Datasource[" + elasticSearch + "] is not configured. please check your configs.",e);
		}
		if (elasticSearchSink == null)
			throw new ElasticSearchException("Elasticsearh Datasource[" + elasticSearch + "] is not configured. please check your configs.");
		return elasticSearchSink.getRestClientUtil();
	}

 
	/**
	 * 加载query dsl配置文件，在默认的es服务器上执行所有操作
	 * @param configFile
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String configFile){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		if(elasticSearchSink == null)
			throw new ElasticSearchException("Elasticsearh Datasource[default] is not configured. please check your configs.");
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}

	public static AOPTemplateContainerImpl getAOPTemplateContainerImpl(String dslpath){
		init();
		ESSOAFileApplicationContext essoaFileApplicationContext = new ESSOAFileApplicationContext(configHolder,dslfileMappingDir,dslpath);
		essoaFileApplicationContext.initProviderManager();
		AOPTemplateContainerImpl aopTemplateContainer = new AOPTemplateContainerImpl(configHolder,dslfileMappingDir,essoaFileApplicationContext);
		return aopTemplateContainer;
	}
	public static List<String> getTemplateFiles()
	{

		return configHolder.getTemplateFiles();
	}
	/**
	 * 加载templateContainer管理的query dsl配置，在默认的es服务器上执行所有操作
	 * @param templateContainer
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(BaseTemplateContainerImpl templateContainer){
//		ElasticSearch elasticSearchSink = context.getTBeanObject(DEFAULT_SEARCH, ElasticSearch.class);
		init();
		if(elasticSearchSink == null)
			throw new ElasticSearchException("Elasticsearh Datasource[default] is not configured. please check your configs.");
		return elasticSearchSink.getConfigRestClientUtil(templateContainer);
	}

	/**
	 * 加载query dsl配置文件，在elasticSearch参数对应的es服务器上执行所有操作
	 * @param elasticSearch
	 * @param configFile
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String elasticSearch,String configFile){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		if(elasticSearchSink == null)
			throw new ElasticSearchException("Elasticsear Datasource["+elasticSearch + "] is not configured. please check your configs.");
		return elasticSearchSink.getConfigRestClientUtil(configFile);
	}
	/**
	 * 加载templateContainer管理的query dsl配置，在elasticSearch参数对应的es服务器上执行所有操作
	 * @param elasticSearch
	 * @param templateContainer
	 * @return
	 */
	public static ClientInterface getConfigRestClientUtil(String elasticSearch,BaseTemplateContainerImpl templateContainer){
		ElasticSearch elasticSearchSink = getElasticSearchSink( elasticSearch);
		if(elasticSearchSink == null)
			throw new ElasticSearchException("Elasticsear Datasource["+elasticSearch + "] is not configured. please check your configs.");
		return elasticSearchSink.getConfigRestClientUtil(templateContainer);
	}

	/**
	 * 管理接口：添加rest服务器
	 * @param hosts
	 */
	public static void addHttpServer(List<String> hosts){

	}
	public static ConfigHolder getConfigHolder(){
		return configHolder;
	}

}
