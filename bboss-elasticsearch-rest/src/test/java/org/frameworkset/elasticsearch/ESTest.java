package org.frameworkset.elasticsearch;

import com.frameworkset.util.FileUtil;
import com.frameworkset.util.SimpleStringUtil;
import org.apache.http.config.SocketConfig;
import org.frameworkset.elasticsearch.client.ClientInterface;
import org.frameworkset.elasticsearch.client.ClientUtil;
import org.frameworkset.elasticsearch.entity.IndexField;
import org.frameworkset.elasticsearch.serial.CharEscapeUtil;
import org.frameworkset.elasticsearch.serial.SerialUtil;
import org.frameworkset.spi.DefaultApplicationContext;
import org.frameworkset.spi.remote.http.MapResponseHandler;
import org.frameworkset.spi.remote.http.StringResponseHandler;
import org.frameworkset.util.FastDateFormat;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
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
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
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
		
		ClientInterface clientUtil = elasticSearchSink.getRestClientUtil();
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
		
		ClientInterface clientUtil = elasticSearchSink.getRestClientUtil();
		String entiry = "{\"query\" : {\"term\" : { \"rpc\" : \"content.page\" }}}";
		String response = (String) clientUtil.executeRequest("trace-*/_search",entiry);
		
		System.out.println(response);
	}
	@Test
	public void testConfig() throws ParseException{
		
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
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

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("org/frameworkset/elasticsearch/ESTracesMapper.xml");
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

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("org/frameworkset/elasticsearch/ESTracesMapper.xml");
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
	public void cleanAllXPackIndices(){
//		ClientInterface clientInterface = ElasticSearchHelper.getRestClientUtil();
//		clientInterface.cleanAllXPackIndices();
		ClientInterface eventClientUtil = ElasticSearchHelper.getRestClientUtil();
		System.out.println(eventClientUtil.getIndice(".security"));
		System.out.println(eventClientUtil.cleanAllXPackIndices());
	}

	@Test
	public void testTempate() throws ParseException{

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTemplate.xml");
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

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTemplate.xml");
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
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		System.out.println(clientUtil.getIndice("demo-"+date));
		clientUtil.dropIndice("demo-"+date);
	}
	@Test
	public void testAddDateDocumentByTemplate() throws ParseException{
		testGetmapping();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		Demo demo = new Demo();
		demo.setDemoId(5l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo");
		demo.setContentbody("this is content body");
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
	public void testBulkAddDateDocumentByTemplate() throws ParseException{
		testGetmapping();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
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

	@Test
	public void testBulkAddDateDocument() throws ParseException{
		testCreateDemoMapping();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
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

		//批量创建文档
		String response = clientUtil.addDateDocuments("demo",//索引表
				"demo",//索引类型
				demos,"refresh=true");

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		response = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"2");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);

		demo = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"3",//文档id
				Demo.class);
	}

	@Test
	public void testBulkAddAndUpdateDateDocuments() throws ParseException{
		testCreateDemoMapping();
		SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd");
		String date = format.format(new Date());
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
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

		//批量创建文档
		String response = clientUtil.addDateDocuments("demo",//索引表
				"demo",//索引类型
				demos);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		//批量更新文档
		demo.setContentbody("updated");
		response = clientUtil.updateDocuments("demo-"+date,"demo",demos);
		System.out.println("updateDateDocument-------------------------");

		System.out.println(response);
		response = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"2");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);

		demo = clientUtil.getDocument("demo-"+date,//索引表
				"demo",//索引类型
				"3",//文档id
				Demo.class);
	}


	@Test
	public void testCreateDemoMapping(){

		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		try {
			//获取索引表结构
			System.out.println(clientUtil.getIndice("demo"));
			//删除索引表结构
			System.out.println(clientUtil.dropIndice("demo"));
		} catch (ElasticSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//创建索引表结构
		System.out.println(clientUtil.createIndiceMapping("demo","createDemoIndice"));

		System.out.println(clientUtil.getIndice("demo"));
		
		System.out.println(clientUtil.getIndice("demo"));
		
		System.out.println(clientUtil.getIndice("demo"));
	}
	@Test
	public void testAddDocumentByTemplate() throws ParseException{
		testCreateDemoMapping();
		org.apache.http.impl.io.SessionInputBufferImpl s;
		SocketConfig dd;
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		Demo demo = new Demo();
		demo.setDemoId(5l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo");
		 
		demo.setContentbody(FileUtil.getFileContent(new File("E:/workspace/bbossgroups/bboss-elastic/bboss-elasticsearch-rest/src/test/java/org/frameworkset/elasticsearch/ESTest.java")));
		//创建文档
		String response = clientUtil.addDocument("demo",//索引表
				"demo",//索引类型
				"createDemoDocument",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
				demo);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		response = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"5");
		System.out.println("getDocument-------------------------");
		System.out.println(response);

		demo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"5",//文档id
				Demo.class);
	}

	@Test
	public void testAddDocument() throws ParseException{
		testCreateDemoMapping();
		ClientInterface clientUtil = ElasticSearchHelper.getRestClientUtil();
		Demo demo = new Demo();
		demo.setDemoId(5l);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo");

		demo.setContentbody(FileUtil.getFileContent(new File("E:/workspace/bbossgroups/bboss-elastic/bboss-elasticsearch-rest/src/test/java/org/frameworkset/elasticsearch/ESTest.java")));
		//创建文档
		String response = clientUtil.addDocument("demo",//索引表
				"demo",//索引类型
				demo);

		System.out.println("addDateDocument-------------------------");
		System.out.println(response);

		response = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"5");//文档id
		System.out.println("getDocument-------------------------");
		System.out.println(response);

		demo = clientUtil.getDocument("demo",//索引表
				"demo",//索引类型
				"5",//文档id
				Demo.class);
	}

	@Test
	public void testBulkAddDocumentByTemplate() {
		testCreateDemoMapping();
		String response = null;
		ClientInterface clientUtil = ElasticSearchHelper.getConfigRestClientUtil("estrace/ESTracesMapper.xml");
		List<Demo> demos = new ArrayList<>();
		long starttime = System.currentTimeMillis();
		for(int j = 0 ; j < 1 ; j ++){
			int start = j * 10000;
			for(int i = start; i < 10000+start; i ++){
				Demo demo = new Demo();
				demo.setDemoId(i);
				demo.setAgentStarttime(new Date());
				demo.setApplicationName("blackcatdemo"+i);
				demo.setContentbody("this is content body中文"+i);
				demos.add(demo);
			}
			//创建文档
			response = clientUtil.addDocuments("demo",//索引表
					"demo",//索引类型
					"createDemoDocument",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
					demos);

			System.out.println("addDateDocument-------------------------");
			System.out.println(response);
			demos.clear();
		}
		long endtime = System.currentTimeMillis();
		System.out.println(endtime - starttime);
		

	 

		
//
//		response = clientUtil.getDocument("demo",//索引表
//				"demo",//索引类型
//				"2");
//		System.out.println("getDocument-------------------------");
//		System.out.println(response);
//
//		Demo demo = clientUtil.getDocument("demo",//索引表
//				"demo",//索引类型
//				"3",//创建文档对应的脚本名称，在estrace/ESTracesMapper.xml中配置
//				Demo.class);
	}


	@Test
	public void testJsonEscape(){
		Demo demo = new Demo();
		demo.setDemoId(10);
		demo.setAgentStarttime(new Date());
		demo.setApplicationName("blackcatdemo");
		demo.setContentbody("成家宁,河北秦皇岛 移动^A电话18713518970");
		demo.setSfiled("aaa");
		demo.setSfiled1("cccc");
		demo.setTestJsonSerialize("aaaa");
		String valur = SerialUtil.object2json(demo);

		System.out.println(valur);
		String v1 = "{\"type\":null,\"id\":null,\"sfiled\":\"fields\",\"sfiled1\":\"fields\",\"fields\":null,\"version\":0,\"index\":null,\"highlight\":null,\"sort\":null,\"score\":0,\"demoId\":10,\"contentbody\":\"成家宁,河北秦皇岛 移动^A电话18713518970\",\"agentStarttime\":\"2017-11-18 04:31:38.229\",\"applicationName\":\"blackcatdemo\"}";
		demo = SimpleStringUtil.json2Object(v1,Demo.class);

		System.out.println( );

	}

	@Test
	public void testCharEscapeUtil(){
		StringWriter writer = new StringWriter();
		CharEscapeUtil charEscapeUtil = new CharEscapeUtil(writer,0);
		
			charEscapeUtil.writeString("成家宁,河北秦皇岛 移动^A电话18713518970",false);
			charEscapeUtil.writeString("  $ ^F^HB ^L  $",false);
			charEscapeUtil.writeString("( ^E`!a",false);
			charEscapeUtil.writeString(FileUtil.getFileContent(new File("E:/workspace/bbossgroups/bboss-elastic/bboss-elasticsearch-rest/src/test/java/org/frameworkset/elasticsearch/ESTest.java")),false);
			 
			System.out.println(writer.toString());
			
			
		

	}


}
