package org.frameworkset.elasticsearch;

import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.elasticsearch.entity.IndexField;
import org.frameworkset.spi.DefaultApplicationContext;
import org.frameworkset.spi.remote.http.MapResponseHandler;
import org.frameworkset.spi.remote.http.StringResponseHandler;
import org.frameworkset.util.FastDateFormat;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ESTest {

	public ESTest() {
		// TODO Auto-generated constructor stub
	}
	@Test
	public void testFastDateFormat() throws ParseException{
		String data = "2005-01-10 12:00:00";
		String format = "yyyy-MM-dd HH:mm:ss";
		FastDateFormat df = FastDateFormat.getInstance(format,TimeZone.getTimeZone("Asia/Shanghai"));
		Object ojb = df.parseObject(data);
		System.out.println();
	}


	@Test
	public void testQueryDocMapping(){
		ClientUtil clientUtil = ElasticSearchHelper.getRestClientUtil();
		System.out.println(clientUtil.getIndexMapping("trace-*",true));
		final List<IndexField> fields = clientUtil.getIndexMappingFields("trace-*","trace");
		System.out.println(fields.size());
	}
	@Test
	public void createIndex(){

	}
	@Test
	public void test() throws Exception{
		DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
		ElasticSearch elasticSearchSink = context.getTBeanObject("elasticSearch", ElasticSearch.class);
//		ElasticSearch restelasticSearchSink = context.getTBeanObject("restelasticSearch", ElasticSearch.class);
		
		ClientUtil clientUtil = elasticSearchSink.getRestClientUtil();
		String entity = "{"+
    "\"aggs\": {"+
    "\"top_tags\": {"+
		    "\"terms\": {"+
		    "\"field\": \"rpc.keyword\","+
		    "\"size\": 30"+
		    "},"+
    "\"aggs\": {"+
		    "\"top_sales_hits\": {"+
		    "\"top_hits\": {"+
		    "\"sort\": ["+
		               "{"+
    "\"collectorAcceptTime\": {"+
		            	    "\"order\": \"desc\""+
		            	        "}"+
    "}"+
    "],"+
    "\"_source\": {"+
		            	    "\"includes\": [ \"collectorAcceptTime\", \"rpc\" ]"+
		            	    	    "},"+
    "\"size\" : 1"+
    "}"+
    "}"+
    "}"+
    "}"+
    "}"+
    "}";
		String response = (String) clientUtil.executeRequest("trace-*/_search?size=0",entity);
		
		System.out.println(response);
		
	}
	@Test
	public void querey() throws Exception
	{
		DefaultApplicationContext context = DefaultApplicationContext.getApplicationContext("conf/elasticsearch.xml");
		ElasticSearch elasticSearchSink = context.getTBeanObject("elasticSearch", ElasticSearch.class);
//		ElasticSearch restelasticSearchSink = context.getTBeanObject("restelasticSearch", ElasticSearch.class);
		
		ClientUtil clientUtil = elasticSearchSink.getRestClientUtil();
		String entiry = "{\"query\" : {\"term\" : { \"rpc\" : \"content.page\" }}}";
		String response = (String) clientUtil.executeRequest("trace-*/_search",entiry);
		
		System.out.println(response);
	}
	@Test
	public void testConfig() throws ParseException{
		
		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
		traceExtraCriteria.setApplication("testweb1");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
		traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-10 00:00:00").getTime());
		 String data = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new StringResponseHandler());
	        System.out.println("------------------------------");
	        System.out.println(data);
	        System.out.println("------------------------------");
	        
	        Map<String,Object> response = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new MapResponseHandler());
	        if(response.containsKey("error")){
	            return ;
	        }
	}
	
	@Test
	public void testSearh() throws ParseException{

		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("org/frameworkset/elasticsearch/ESTracesMapper.xml");
		TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
		traceExtraCriteria.setApplication("testweb1");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
		traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-10 00:00:00").getTime());
		String data = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new StringResponseHandler());
		System.out.println("------------------------------");
		System.out.println(data);
		System.out.println("------------------------------");

		Map<String,Object> response = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new MapResponseHandler());
		if(response.containsKey("error")){
			return ;
		}
	}

	@Test
	public void testSearhHits() throws ParseException{

		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("org/frameworkset/elasticsearch/ESTracesMapper.xml");
		TraceExtraCriteria traceExtraCriteria = new TraceExtraCriteria();
		traceExtraCriteria.setApplication("testweb1");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		traceExtraCriteria.setStartTime(dateFormat.parse("2017-09-02 00:00:00").getTime());
		traceExtraCriteria.setEndTime(dateFormat.parse("2017-09-10 00:00:00").getTime());
		String data = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new StringResponseHandler());
		System.out.println("------------------------------");
		System.out.println(data);
		System.out.println("------------------------------");
		Map<String,Object> response = clientUtil.executeRequest("trace-*/_search","queryPeriodsTopN",traceExtraCriteria,new MapResponseHandler());
		if(response.containsKey("error")){
			return ;
		}
	}

	@Test
	public void testTempate() throws ParseException{

		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTemplate.xml");
		//创建模板
		String response = clientUtil.createTempate("demotemplate_1",//模板名称
				"demoTemplate");//模板对应的脚本名称，在estrace/ESTemplate.xml中配置
		System.out.println("createTempate-------------------------");
		System.out.println(response);
		//获取模板
		/**
		 * 指定模板
		 * /_template/demoTemplate_1
		 * /_template/demoTemplate*
		 * 所有模板 /_template
		 *
		 */
		String template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET-------------------------");
		System.out.println(template);
		//删除模板
		template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_DELETE);
		System.out.println("HTTP_DELETE-------------------------");
		System.out.println(template);

		template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET after delete-------------------------");
		System.out.println(template);
	}


	@Test
	public void testCreateTempate() throws ParseException{

		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTemplate.xml");
		//创建模板
		String response = clientUtil.createTempate("demotemplate_1",//模板名称
				"demoTemplate");//模板对应的脚本名称，在estrace/ESTemplate.xml中配置
		System.out.println("createTempate-------------------------");
		System.out.println(response);
		//获取模板
		/**
		 * 指定模板
		 * /_template/demoTemplate_1
		 * /_template/demoTemplate*
		 * 所有模板 /_template
		 *
		 */
		String template = clientUtil.executeHttp("/_template/demotemplate_1",ClientUtil.HTTP_GET);
		System.out.println("HTTP_GET-------------------------");
		System.out.println(template);

	}
	@Test
	public void testGetmapping(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		System.out.println(clientUtil.getIndice("demo-"+date));
		clientUtil.dropIndice("demo-"+date);
	}
	@Test
	public void testAddDateDocument() throws ParseException{
		testGetmapping();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		Demo demo = new Demo();
		demo.setDemoId(5l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo");
		demo.setContentbody("this is content body");
		org.joda.time.format.DateTimeParserBucket s;
		//创建模板
		String response = clientUtil.addDateDocument("demo",//索引表
				"demo",//索引类型
				"createDemoDocument",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
				demo);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		response = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"5");
		System.out.println("getDocument-------------------------");
		System.out.println(response);

		demo = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"5",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
				Demo.class);
	}
	
	@Test
	public void testBulkAddDateDocument() throws ParseException{
		testGetmapping();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientUtil clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		List<Demo> demos = new ArrayList<>();
		Demo demo = new Demo();
		demo.setDemoId(2l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo2");
		demo.setContentbody("this is content body2");
		demos.add(demo);

		demo = new Demo();
		demo.setDemoId(3l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo3");
		demo.setContentbody("this is content body3");
		demos.add(demo);

		//创建模板
		String response = clientUtil.addDateDocuments("demo",//索引表
				"demo",//索引类型
				"createDemoDocument",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
				demos);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		response = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"2");
		System.out.println("getDocument-------------------------");
		System.out.println(response);

		demo = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"3",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
				Demo.class);
	}





}
